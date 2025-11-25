import { history } from "umi";
import { StrUtil, UrlUtil } from "../utils";

/**
 * 页面相关的工具类，主要用于路由、URL参数和页面跳转操作。
 */
export class PageUtils {

    /**
     * 重定向到登录页，并携带当前页面的URL作为回跳参数。
     */
    static redirectToLogin(): void {
        const url = PageUtils.currentUrl();
        // 对URL进行编码，确保参数安全传递
        const encodedUrl = encodeURIComponent(url);
        history.push(`/login?redirect=${encodedUrl}`);
    }

    /**
     * @deprecated 请使用 currentParams() 代替。
     * 获取当前 URL 的查询参数对象。
     * @returns {Record<string, string | undefined>} 包含查询参数的键值对对象。
     */
    static currentLocationQuery(): Record<string, string | undefined> {
        return this.currentParams();
    }

    /**
     * 获取当前 URL 的查询参数对象。
     * @returns {Record<string, string | undefined>} 包含查询参数的键值对对象。
     */
    static currentParams(): Record<string, string | undefined> {
        const url = window.location.href;

        // 检查是否存在查询参数
        const hasQuery = url.indexOf('?') > 0;
        if (!hasQuery) {
            return {};
        }

        // 提取问号后面的查询字符串
        const search = url.substring(url.indexOf('?') + 1);
        const kvs = search.split('&');

        const rs: Record<string, string | undefined> = {};
        for (const kv of kvs) {
            const kvArr = kv.split('=');
            const k = kvArr[0];
            let v = kvArr[1];

            // 值存在时才进行解码
            if (v) {
                // 使用 decodeURIComponent 更安全地处理 URL 编码的值
                try {
                    v = decodeURIComponent(v);
                } catch (e) {
                    console.error("Failed to decode URL parameter value:", v, e);
                    // 解码失败时保留原值
                }
            } else {
                v = undefined; // 如果值为空，设置为 undefined
            }
            rs[k] = v;
        }

        return rs;
    }

    /**
     * 获取当前 URL 的路径名（不带查询参数和 Hash 符号 #）。
     * 例如："http://localhost:8000/#/login?id=1" -> "/login"
     * @returns {string} 不带参的路径。
     */
    static currentPathname(): string {
        // window.location.hash.substring(1) 得到 "/login?id=1"
        const path = window.location.hash.substring(1);
        // 假设 StrUtil.subBefore(str, separator) 存在并返回 separator 之前的部分
        // 需要确保 StrUtil 模块中存在 subBefore 方法
        return StrUtil.subBefore(path, '?');
    }

    /**
     * @deprecated 请使用 currentUrl() 代替。
     * 获取 hash 后的完整路径 (带参数)。
     * @returns {string} 例如："/login?id=1"
     */
    static currentPath(): string {
        // window.location.hash.substring(1) 得到 "/login?id=1"
        return window.location.hash.substring(1);
    }

    /**
     * 获取 hash 后的完整路径 (带参数)。
     * @returns {string} 例如："/login?id=1"
     */
    static currentUrl(): string {
        return window.location.hash.substring(1);
    }

    /**
     * 当前路由的最后一个斜杠后面的单词，通常用于基于路径的路由。
     * 例如："/user/detail/123" -> "123"
     * @returns {string | undefined} 路径的最后一部分或 undefined。
     */
    static currentPathnameLastPart(): string | undefined {
        const path = this.currentPathname();
        // 假设 StrUtil.subAfterLast(str, separator) 存在并返回 separator 之后的部分
        return StrUtil.subAfterLast(path, '/') || undefined;
    }

    /**
     * 打开一个新页面，可选择添加一个 '_label' 参数。
     * @param path 要跳转的路径。
     * @param label 可选，用于在 URL 中添加一个 '_label' 参数。
     */
    static open(path: string, label?: string): void {
    let targetPath = path;
    if(label) {
        // 假设 UrlUtil.setParam(url, key, value) 存在并返回设置参数后的 URL
        targetPath = UrlUtil.setParam(targetPath, '_label', label);
    }
    history.push(targetPath);
}

/**
 * 打开一个不带菜单、Header 等布局元素的页面。
 * 通过在 URL 中添加 '_noLayout=true' 参数实现。
 * @param path 要跳转的路径。
 */
static openNoLayout(path: string): void {
    // 假设 UrlUtil.setParam(url, key, value) 存在
    const targetPath = UrlUtil.setParam(path, '_noLayout', true);
    history.push(targetPath);
}

/**
 * 获取当前 URL 参数中名为 '_label' 的值。
 * @returns {string | undefined} '_label' 参数的值。
 */
static currentLabel(): string | undefined {
    // currentParams 返回 Record<string, string | undefined>
    return this.currentParams()['_label'];
}

/**
 * 发送一个自定义事件来关闭当前页面。
 * 依赖外部监听 'close-page-event' 事件的处理机制。
 */
static closeCurrent(): void {
    const event = new CustomEvent<{ url: string }>('close-page-event', {
        detail: { url: PageUtils.currentUrl() }
    });
    document.dispatchEvent(event);
}
}
