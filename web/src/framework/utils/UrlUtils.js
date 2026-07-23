import {StringUtils} from './StringUtils';

export class UrlUtils {
    static getParams(url = null) {
        let targetUrl;
        if (url === null || url === undefined) {
            targetUrl = location.href;
        } else {
            targetUrl = url;
        }

        if (!StringUtils.contains(targetUrl, '?')) return {};
        const search = StringUtils.subAfter(targetUrl, '?');
        const params = new URLSearchParams(search);
        const result = {};
        for (const [key, value] of params.entries()) {
            result[key] = value;
        }
        return result;
    }

    static getPathname(url) {
        if (!url) return null;
        return StringUtils.subBefore(url, '?');
    }

    static paramsToSearch(params) {
        if (!params) return "";
        const buffer = [];
        for (const k in params) {
            if (Object.prototype.hasOwnProperty.call(params, k)) {
                const v = params[k];
                if (v !== null && v !== undefined) {
                    buffer.push(`${k}=${v}`);
                }
            }
        }
        return buffer.join('&');
    }

    static setParam(url, key, value) {
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
    static getWebsocketBaseUrl() {
        const protocol = location.protocol === 'http:' ? 'ws:' : 'wss:';
        return `${protocol}//${location.host}`;
    }

    /**
     * 为路径添加 SERVLET_CONTEXT 前缀
     */
    static contextPath(path) {
        const base = (typeof SERVLET_CONTEXT !== 'undefined' && SERVLET_CONTEXT) || '';
        return base + path;
    }
}
