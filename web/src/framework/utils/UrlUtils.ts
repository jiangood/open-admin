import {StringUtils} from './StringUtils';

export class UrlUtils {
    static getParams(url: string | null = null): Record<string, string> {
        let targetUrl: string;
        if (url === null || url === undefined) {
            targetUrl = location.href;
        } else {
            targetUrl = url;
        }

        if (!StringUtils.contains(targetUrl, '?')) return {};
        const search = StringUtils.subAfter(targetUrl, '?');
        const params = new URLSearchParams(search);
        const result: Record<string, string> = {};
        for (const [key, value] of params.entries()) {
            result[key] = value;
        }
        return result;
    }

    static getPathname(url: string | null | undefined): string | null {
        if (!url) return null;
        return StringUtils.subBefore(url, '?');
    }

    static paramsToSearch(params: Record<string, string | number | boolean | null | undefined> | null | undefined): string {
        if (!params) return "";
        const buffer: string[] = [];
        for (const k in params) {
            if (Object.hasOwn(params, k)) {
                const v = params[k];
                if (v !== null && v !== undefined) {
                    buffer.push(`${k}=${v}`);
                }
            }
        }
        return buffer.join('&');
    }

    static setParam(url: string, key: string, value: string | number | boolean | null | undefined): string {
        const currentParams = this.getParams(url);
        if (value === null || value === undefined) {
            delete currentParams[key];
        } else {
            currentParams[key] = String(value);
        }
        const pathname = this.getPathname(url);
        const newSearch = this.paramsToSearch(currentParams);
        if (newSearch.length > 0) {
            return `${pathname}?${newSearch}`;
        }
        return pathname || url;
    }

    /**
     * 根据当前页面协议和主机名构建 WebSocket 基础 URL
     */
    static getWebsocketBaseUrl(): string {
        const protocol = location.protocol === 'http:' ? 'ws:' : 'wss:';
        return `${protocol}//${location.host}`;
    }

    /**
     * 为路径添加 SERVLET_CONTEXT 前缀
     */
    static contextPath(path: string): string {
        const base = import.meta.env.VITE_SERVER_SERVLET_CONTEXT_PATH;
        if (base === '/' || base === '') return path;
        return base.replace(/\/+$/, '') + path;
    }
}
