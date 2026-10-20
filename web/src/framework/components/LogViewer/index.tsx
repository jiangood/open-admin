import React from "react";
import { Alert, Spin, Typography } from "antd";
import { HttpClient } from "../../utils";
import { getToken } from "../../config";

/** 增量拉取时每次最多读取的字节数（与后端默认值一致） */
const DEFAULT_LENGTH = 256 * 1024;

/** 运行中日志的默认刷新间隔（毫秒） */
const DEFAULT_POLL_INTERVAL = 1500;

export interface LogViewerProps {
    /** 日志 key，如 `job/{logId}`；与后端 `admin/sys/log/{*key}` 对应 */
    logKey?: string;
    /** 任务是否仍在运行：为 true 时按 `pollInterval` 增量轮询，false 时只读一次 */
    running?: boolean;
    /** 运行中的轮询间隔（毫秒），默认 1500 */
    pollInterval?: number;
    /** 可视高度（像素），默认 500 */
    height?: number;
    /** 是否自动滚动到底部，默认 true；用户上滑查看历史后不再强制跟随 */
    autoFollow?: boolean;
    /** 内容为空时的提示 */
    emptyText?: React.ReactNode;
    className?: string;
    style?: React.CSSProperties;
}

interface LogViewerState {
    content: string;
    nextOffset: number;
    loading: boolean;
    error: string | null;
    eof: boolean;
}

/**
 * 文件日志查看器：按字节偏移增量读取日志文件。
 * <p>
 * 运行中的任务（`running`）按 `pollInterval` 反复拉取新增内容并追加；任务结束后停止轮询，
 * 且会补拉一次剩余内容。日志很长时也不会每次整文件重读。
 * <p>
 * 使用方式（定时任务日志）：
 * ```tsx
 * <LogViewer logKey={`job/${logId}`} running={isRunning} />
 * ```
 */
export class LogViewer extends React.Component<LogViewerProps, LogViewerState> {

    state: LogViewerState = {
        content: "",
        nextOffset: 0,
        loading: false,
        error: null,
        eof: false,
    };

    private containerRef = React.createRef<HTMLPreElement>();
    private timer: ReturnType<typeof setTimeout> | null = null;
    private mounted = false;
    /** 用户是否仍处于底部（跟随最新日志）；上滑后置为 false */
    private follow = true;
    /** 请求进行中标记，避免慢请求叠加 */
    private fetching = false;
    /** 请求进行中被跳过的拉取请求，结束后立即补一次 */
    private pendingFetch = false;
    /** 请求代号：切换 logKey / 重新加载后，丢弃旧请求的迟到响应 */
    private generation = 0;

    componentDidMount() {
        this.mounted = true;
        this.reload();
    }

    componentDidUpdate(prevProps: LogViewerProps) {
        if (prevProps.logKey !== this.props.logKey) {
            this.reload();
            return;
        }
        // 运行状态变化（尤其运行中 -> 结束）时补拉一次，拿到最后一段日志
        if (prevProps.running !== this.props.running) {
            this.schedule(0);
        }
    }

    componentWillUnmount() {
        this.mounted = false;
        this.clearTimer();
    }

    /** 从头重新加载 */
    reload = () => {
        this.clearTimer();
        this.generation++;
        this.pendingFetch = false;
        this.follow = true;
        this.setState({ content: "", nextOffset: 0, loading: true, error: null, eof: false },
            () => this.fetchChunk(true));
    };

    private clearTimer() {
        if (this.timer) {
            clearTimeout(this.timer);
            this.timer = null;
        }
    }

    private schedule(delay?: number) {
        if (!this.mounted) {
            return;
        }
        this.clearTimer();
        const running = !!this.props.running;
        // 未运行时只在需要时补拉（delay 显式传入），不做周期性轮询
        if (!running && delay == null) {
            return;
        }
        const interval = this.props.pollInterval ?? DEFAULT_POLL_INTERVAL;
        this.timer = setTimeout(() => this.fetchChunk(false), delay ?? interval);
    }

    private fetchChunk(initial: boolean) {
        const { logKey } = this.props;
        if (!logKey) {
            return;
        }
        if (this.fetching) {
            // 已有请求在途：打完标记，等它结束后立即补一次
            this.pendingFetch = true;
            return;
        }
        this.fetching = true;
        const generation = this.generation;
        const offset = initial ? 0 : this.state.nextOffset;

        HttpClient.get<{ content: string; nextOffset: number; eof: boolean }>(
            `admin/sys/log/${logKey}`,
            { offset, length: DEFAULT_LENGTH },
            { toastError: false }
        ).then(rs => {
            if (!this.mounted || generation !== this.generation) {
                return;
            }
            const chunk = rs.data;
            if (!chunk) {
                this.setState({ loading: false, eof: true });
                return;
            }
            this.setState(prev => ({
                content: initial ? (chunk.content || "") : prev.content + (chunk.content || ""),
                nextOffset: chunk.nextOffset,
                eof: chunk.eof,
                loading: false,
                error: null,
            }), () => {
                this.scrollToBottom();
                if (!chunk.eof) {
                    // 还有剩余内容（超过单次读取上限）：立即续读
                    this.schedule(0);
                } else {
                    // 已读到末尾：运行中按间隔等新内容，已结束则停止
                    this.schedule();
                }
            });
        }).catch(e => {
            if (!this.mounted || generation !== this.generation) {
                return;
            }
            this.setState({ loading: false, error: e?.message || "日志加载失败" });
            // 运行中遇到临时错误（如文件尚未创建）继续重试
            this.schedule();
        }).finally(() => {
            this.fetching = false;
            if (this.pendingFetch) {
                this.pendingFetch = false;
                this.fetchChunk(false);
            }
        });
    }

    private scrollToBottom() {
        const el = this.containerRef.current;
        if (!el || !(this.props.autoFollow ?? true)) {
            return;
        }
        if (!this.follow) {
            return;
        }
        el.scrollTop = el.scrollHeight;
    }

    private handleScroll = () => {
        const el = this.containerRef.current;
        if (!el) {
            return;
        }
        // 距底部 24px 内视为跟随
        this.follow = el.scrollHeight - el.scrollTop - el.clientHeight < 24;
    };

    render() {
        const { height = 500, emptyText = "暂无日志", className, style } = this.props;
        const { content, loading, error } = this.state;
        const token = getToken();

        return <div className={className} style={{ height, position: "relative", ...style }}>
            {error && content.length === 0
                ? <Alert type="error" showIcon message={error}/>
                : <>
                    {loading && content.length === 0 && (
                        <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center" }}>
                            <Spin/>
                        </div>
                    )}
                    {!loading && content.length === 0 && (
                        <Typography.Text type="secondary">{emptyText}</Typography.Text>
                    )}
                    <pre
                        ref={this.containerRef}
                        onScroll={this.handleScroll}
                        style={{
                            margin: 0,
                            height: "100%",
                            overflow: "auto",
                            whiteSpace: "pre-wrap",
                            wordBreak: "break-all",
                            fontSize: 12,
                            lineHeight: 1.6,
                            padding: 12,
                            boxSizing: "border-box",
                            backgroundColor: token.colorBgContainer,
                            color: token.colorText,
                            border: `1px solid ${token.colorBorderSecondary}`,
                            borderRadius: token.borderRadius,
                        }}
                    >{content}</pre>
                </>}
        </div>;
    }
}
