import React from "react";
import { LogViewer } from "../LogViewer";
import { UrlUtils } from "../../utils";
import { Alert, Spin, Typography } from "antd";
import { getToken } from "../../config";

export type TaskLogTransport = "http" | "ws" | "auto";

export interface TaskLogViewerProps {
    /** 任务 logId（不含 task/ 前缀） */
    logId?: string;
    /** 传输方式：默认 http。ws 需要 wsUrl 或可推导的地址 */
    transport?: TaskLogTransport;
    /** HTTP 模式使用的 logKey，默认 task/{logId} */
    logKey?: string;
    /** WebSocket 地址；可为相对路径或完整 ws(s) URL */
    wsUrl?: string;
    /** 任务是否仍在运行（http 轮询/auto 切换参考） */
    running?: boolean;
    /** 运行中的轮询间隔（毫秒），http 模式使用 */
    pollInterval?: number;
    /** 可视高度（像素） */
    height?: number;
    /** 内容为空时的提示 */
    emptyText?: React.ReactNode;
    /** 任务结束回调（ws 连接关闭或检测到结束时触发） */
    onEnd?: () => void;
    className?: string;
    style?: React.CSSProperties;
}

interface WsState {
    content: string;
    loading: boolean;
    error: string | null;
    ended: boolean;
}

/**
 * 任务日志查看器：统一支持 HTTP 轮询与 WebSocket 实时 tail。
 * <p>
 * - http：基于 logviewer 分块读取（logKey 默认 task/{logId}）
 * - ws：连接 wsUrl 实时接收日志文本，连接关闭视为结束
 * - auto：运行中优先 ws（需 wsUrl），否则/结束后回退 http
 */
export class TaskLogViewer extends React.Component<TaskLogViewerProps, WsState> {
    static defaultProps = {
        transport: "http" as TaskLogTransport,
        emptyText: "暂无日志",
    };

    state: WsState = {
        content: "",
        loading: false,
        error: null,
        ended: false,
    };

    private ws: WebSocket | null = null;
    private containerRef = React.createRef<HTMLPreElement>();
    private mounted = false;
    private follow = true;
    private endNotified = false;

    componentDidMount() {
        this.mounted = true;
        this.sync();
    }

    componentDidUpdate(prevProps: TaskLogViewerProps) {
        const mode = this.resolveMode(this.props);
        const prevMode = this.resolveMode(prevProps);
        if (
            prevProps.logId !== this.props.logId ||
            prevProps.wsUrl !== this.props.wsUrl ||
            prevProps.logKey !== this.props.logKey ||
            prevProps.transport !== this.props.transport ||
            (this.props.transport === "auto" && prevProps.running !== this.props.running)
        ) {
            this.resetWs();
            this.sync();
            return;
        }
        // auto 模式下 running 变为 false 且当前是 ws，关闭 ws 后 http 组件会接管
        if (this.props.transport === "auto" && prevProps.running && !this.props.running && mode === "http" && prevMode === "ws") {
            // ws will be closed by resetWs in branch above not taken? no
        }
    }

    componentWillUnmount() {
        this.mounted = false;
        this.resetWs();
    }

    private resolveMode(props: TaskLogViewerProps): "http" | "ws" {
        const transport = props.transport || "http";
        if (transport === "http") return "http";
        if (transport === "ws") return "ws";
        // auto: 运行中且有 wsUrl 时优先 ws
        if (props.running && this.buildWsUrl(props)) return "ws";
        return "http";
    }

    private buildWsUrl(props: TaskLogViewerProps = this.props): string | null {
        const { wsUrl } = props;
        if (!wsUrl) return null;
        let url = wsUrl;
        if (!url.startsWith("ws://") && !url.startsWith("wss://") && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = UrlUtils.getWebsocketBaseUrl() + UrlUtils.contextPath(url);
        } else if (url.startsWith("http://")) {
            url = "ws://" + url.substring(7);
        } else if (url.startsWith("https://")) {
            url = "wss://" + url.substring(8);
        }
        return url;
    }

    private sync() {
        if (this.resolveMode(this.props) === "ws") {
            this.connectWs();
        } else {
            this.resetWs();
        }
    }

    private connectWs() {
        const url = this.buildWsUrl();
        if (!url || !this.props.logId) return;
        this.resetWs(false);
        this.endNotified = false;
        this.follow = true;
        this.setState({ content: "", loading: true, error: null, ended: false });
        try {
            const ws = new WebSocket(url);
            this.ws = ws;
            ws.onopen = () => {
                if (!this.mounted || this.ws !== ws) return;
                this.setState({ loading: false });
            };
            ws.onmessage = (ev) => {
                if (!this.mounted || this.ws !== ws) return;
                const data = typeof ev.data === "string" ? ev.data : "";
                this.setState((prev) => ({ content: prev.content + data, loading: false }), () => this.scrollToBottom());
            };
            ws.onerror = () => {
                if (!this.mounted || this.ws !== ws) return;
                this.setState({ loading: false, error: "日志连接失败" });
            };
            ws.onclose = () => {
                if (this.ws === ws) this.ws = null;
                if (!this.mounted) return;
                this.setState({ loading: false, ended: true }, () => this.notifyEnd());
            };
        } catch (e: any) {
            if (this.mounted) this.setState({ loading: false, error: e?.message || "日志连接失败" });
        }
    }

    private notifyEnd() {
        if (this.endNotified) return;
        this.endNotified = true;
        this.props.onEnd && this.props.onEnd();
    }

    private resetWs(clearState = true) {
        if (this.ws) {
            try {
                this.ws.onclose = null;
                this.ws.onerror = null;
                this.ws.onmessage = null;
                this.ws.close();
            } catch (e) {
                // ignore
            }
            this.ws = null;
        }
        if (clearState && this.mounted) {
            // no-op: state handled by sync/connect
        }
    }

    private scrollToBottom() {
        const el = this.containerRef.current;
        if (!el || !this.follow) return;
        el.scrollTop = el.scrollHeight;
    }

    private handleScroll = () => {
        const el = this.containerRef.current;
        if (!el) return;
        this.follow = el.scrollHeight - el.scrollTop - el.clientHeight < 24;
    };

    renderWs() {
        const { height = 500, emptyText = "暂无日志", className, style } = this.props;
        const { content, loading, error } = this.state;
        const token = getToken();

        return (
            <div className={className} style={{ height, position: "relative", ...style }}>
                {error && content.length === 0 ? (
                    <Alert type="error" showIcon message={error} />
                ) : (
                    <>
                        {loading && content.length === 0 && (
                            <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center" }}>
                                <Spin />
                            </div>
                        )}
                        {!loading && content.length === 0 && !error && (
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
                        >
                            {content}
                        </pre>
                    </>
                )}
            </div>
        );
    }

    render() {
        const { logId, logKey, running, pollInterval, height, emptyText, className, style } = this.props;
        const mode = this.resolveMode(this.props);
        if (mode === "ws") {
            return this.renderWs();
        }
        const resolvedLogKey = logKey || (logId ? "task/" + logId : undefined);
        return (
            <LogViewer
                logKey={resolvedLogKey}
                running={running}
                pollInterval={pollInterval}
                height={height}
                emptyText={emptyText}
                className={className}
                style={style}
            />
        );
    }
}

export default TaskLogViewer;
