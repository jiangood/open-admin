import React from "react";
import { LazyLog, ScrollFollow } from "@melloware/react-logviewer";
import { UrlUtils } from "../../utils";

export type TaskLogTransport = "http" | "ws" | "auto";

export interface TaskLogViewerProps {
    /** 任务 logId（不含 task/ 前缀） */
    logId?: string;
    /** 传输方式：默认 http。ws 需要 wsUrl 或可推导的地址 */
    transport?: TaskLogTransport;
    /** HTTP 模式使用的 logKey；未指定时默认 task/{logId} */
    logKey?: string;
    /** 自定义读取地址，覆盖默认 raw 日志接口 */
    url?: string;
    /** WebSocket 地址；可为相对路径或完整 ws(s) URL */
    wsUrl?: string;
    /** 任务是否仍在运行（影响跟随滚动） */
    running?: boolean;
    /** 运行中的轮询间隔（毫秒，LazyLog 模式下保留兼容，不使用） */
    pollInterval?: number;
    /** 可视高度（像素） */
    height?: number;
    /** 是否自动跟随到底部，默认 true */
    autoFollow?: boolean;
    /** 内容为空时的提示（LazyLog 模式下保留兼容，不单独渲染） */
    emptyText?: React.ReactNode;
    /** 任务结束回调（ws 连接关闭时触发） */
    onEnd?: () => void;
    className?: string;
    style?: React.CSSProperties;
}

/**
 * 任务日志查看器：基于 @melloware/react-logviewer（LazyLog）展示日志。
 * <p>
 * - http：从 raw 文本接口读取日志（logKey 未指定时默认 task/{logId}）
 * - ws：连接 wsUrl 实时接收日志文本
 * - auto：运行中且有 wsUrl 时优先 ws，否则回退 http
 */
export class TaskLogViewer extends React.Component<TaskLogViewerProps> {
    static defaultProps = {
        transport: "http" as TaskLogTransport,
        autoFollow: true,
    };

    private resolveMode(): "http" | "ws" {
        const { transport = "http", running } = this.props;
        if (transport === "http") return "http";
        if (transport === "ws") return "ws";
        if (running && this.buildWsUrl()) return "ws";
        return "http";
    }

    private buildWsUrl(): string | null {
        const { wsUrl } = this.props;
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

    private getHttpUrl(): string | null {
        const { url, logKey, logId } = this.props;
        if (url) return url;
        const resolvedLogKey = logKey || (logId ? "task/" + logId : undefined);
        if (!resolvedLogKey) return null;
        return UrlUtils.contextPath("/admin/sys/log/raw/" + resolvedLogKey);
    }

    render() {
        const { height = 500, running, onEnd, className, style, autoFollow = true } = this.props;
        const mode = this.resolveMode();
        const isWs = mode === "ws";
        const url = isWs ? this.buildWsUrl() : this.getHttpUrl();
        const follow = autoFollow && !!running;

        if (!url) {
            return <div className={className} style={{ height, ...style }} />;
        }

        const key = `${mode}:${url}`;

        return (
            <div className={className} style={{ height, ...style }}>
                <ScrollFollow startFollowing={follow} render={({ follow: following, onScroll }: { follow: boolean; onScroll: () => void }) => (
                    <LazyLog
                        key={key}
                        url={url}
                        height={height}
                        follow={following}
                        enableSearch
                        selectableLines
                        extraLines={1}
                        fetchOptions={isWs ? undefined : { credentials: "include" }}
                        websocket={isWs}
                        websocketOptions={isWs ? { onClose: onEnd } : undefined}
                        onScroll={onScroll}
                    />
                )} />
            </div>
        );
    }
}

export default TaskLogViewer;
