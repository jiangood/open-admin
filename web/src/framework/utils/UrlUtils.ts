// 假设 StrUtil 已经转换为 StrUtils 或有一个对应的类型定义
// 如果 StrUtil 是一个工具对象，这里可能需要导入它
import {StringUtils} from "./StringUtils";

/**
 * URL 相关操作的工具类
 */
export class UrlUtils {

    /**
     * 定义一个类型别名，用于 URL 参数对象
     */
    public type
    ParamsObject = Record<string, string | number | boolean | null | undefined>;

    /**
     * 获取 URL 的参数对象。
     * 如果不传参数，则使用当前路径 (location.href)。
     * @param url 完整的 URL 字符串，可选。
     * @returns 包含 URL 参数的键值对对象。
     */
    public static getParams(url: string | null = null): UrlUtils.ParamsObject {
        // 1. 确定要解析的 URL
        let targetUrl: string;
        if (url === null || url === undefined) {
            targetUrl = location.href;
        } else {
            targetUrl = url;
        }

        // 2. 检查是否有查询参数
        if (!StringUtils.contains(targetUrl, '?')) {
            return {};
        }

        // 3. 提取查询字符串 (问号后面的部分)
        // 假设 StrUtil.subAfter 能够正确获取 '?' 之后的部分
        const search = StringUtils.subAfter(targetUrl, '?');

        // 4. 使用 URLSearchParams 进行解析 (现代浏览器环境)
        // 注意：在某些旧环境或非浏览器环境中，可能需要不同的解析逻辑
        const params = new URLSearchParams(search);

        // 5. 转换为普通对象
        const result: UrlUtils.ParamsObject = {} as UrlUtils.ParamsObject;
        for (const [key, value] of params.entries()) {
            // URLSearchParams 的值总是 string，因此这里赋值为 string
            result[key] = value;
        }

        return result;
    }

    /**
     * 获取不带参数的基础 URL (pathname)。
     * @param url 完整的 URL 字符串。
     * @returns 不包含查询参数的 URL 部分，如果 url 为 null/undefined，则返回 null。
     */
    public static getPathname(url: string | null | undefined): string | null {
        if (!url) {
            return null;
        }
        // 假设 StrUtil.subBefore 能够正确获取 '?' 之前的部分
        return StringUtils.subBefore(url, '?');
    }

    /**
     * 将参数对象转换为 URL 中的查询字符串（例如 k1=v1&k2=v2）。
     * @param params 包含参数的键值对对象。
     * @returns 查询字符串，不包含开头的问号 (?)。如果 params 为空，则返回空字符串。
     */
    public static paramsToSearch(params: UrlUtils.ParamsObject | null | undefined): string {
        if (!params) {
            return "";
        }

        const buffer: string[] = [];
        // 迭代对象的自身属性
        for (const k in params) {
            // 确保只处理对象自身的属性，排除原型链上的属性
            if (Object.prototype.hasOwnProperty.call(params, k)) {
                const v = params[k];
                // 忽略值为 null 或 undefined 的参数，或根据需要进行编码
                if (v !== null && v !== undefined) {
                    // 这里对值进行简单的连接，实际应用中建议使用 encodeURIComponent
                    buffer.push(`${k}=${v}`);
                }
            }
        }
        return buffer.join('&');
    }

    /**
     * 设置或删除 URL 中的一个参数，并返回新的 URL。
     * @param url 原始 URL 字符串。
     * @param key 要设置或删除的参数名。
     * @param value 要设置的参数值。如果传入 null 或 undefined，则删除该参数。
     * @returns 带有新参数的新 URL 字符串。
     */
    public static setParam(url: string, key: string, value: string | number | boolean | null | undefined): string {
        // 1. 获取现有参数
        const currentParams = this.getParams(url);

        // 2. 根据 value 设置或删除参数
        if (value === null || value === undefined) {
            delete currentParams[key];
        } else {
            // 强制转换为 string 存储，以符合 URL 参数的惯例
            currentParams[key] = String(value);
        }

        // 3. 获取基础路径
        const pathname = this.getPathname(url);

        // 4. 将新的参数对象转换为查询字符串
        const newSearch = this.paramsToSearch(currentParams);

        // 5. 组合新的 URL
        if (newSearch.length > 0) {
            return `${pathname}?${newSearch}`;
        }
        // 如果没有参数，则不带问号
        return pathname || url; // 如果 pathname 为 null，则返回原始 url
    }

    /**
     * @deprecated 使用 setParam 代替。
     * 设置或替换 URL 中的一个参数，功能与 setParam 相同。
     * @param url 原始 URL 字符串。
     * @param key 要替换的参数名。
     * @param value 要替换的参数值。
     * @returns 带有新参数的新 URL 字符串。
     */
    public static replaceParam(url: string, key: string, value: string | number | boolean | null | undefined): string {
        // 直接调用 setParam，并返回结果
        return this.setParam(url, key, value);
    }

    /**
     * 连接两个路径片段，确保中间只有一个斜杠 (/)。
     * @param path1 第一个路径片段。
     * @param path2 第二个路径片段。
     * @returns 连接后的路径字符串。
     */
    public static join(path1: string, path2: string): string {
        // 假设 StrUtil.removeSuffix/removePrefix 能够处理字符串前后的斜杠
        const cleanPath1 = StringUtils.removeSuffix(path1, "/");
        const cleanPath2 = StringUtils.removePrefix(path2, "/");

        return `${cleanPath1}/${cleanPath2}`;
    }
}
