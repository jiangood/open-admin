import axios from "axios";
import type {AxiosProgressEvent, AxiosRequestConfig} from "axios";
import qs from 'qs';
import {message as messageApi} from "antd";
import {EventBus} from "./EventBus";

/**
 * 回调式 HTTP 工具类（纯回调，所有方法均不返回 Promise）
 *
 * 核心规则：
 * - 传了 error 回调 → 异常完全交给调用方，框架不自动弹错
 * - 没传 error 回调 → 框架自动 console.warn + message.error
 * - 成功永不自动弹 toast，是否提示由业务方在 success 回调里自行决定
 * - error 回调统一收到 AjaxError {code, message}：业务失败 code 为后端业务码，
 *   网络异常 code 为 HTTP 状态码，message 始终为人类可读提示
 * - HTTP 401 或业务 code=401 均广播 loginExpired（全局关注点），提示仍按 error 回调有无决定
 *
 * 用法：
 * ```ts
 * // 失败自动弹错；成功自控提示
 * HttpClient.post('admin/sysUser/delete', {id}, null, () => {
 *     messageApi.success('删除成功');
 *     reload();
 * });
 *
 * // 失败由调用方接管，不弹
 * HttpClient.get('admin/sysUser/page', params, (data) => render(data), (e) => myHandle(e));
 *
 * // 静默探测：成功失败都不弹
 * HttpClient.get('admin/public/site-info', null, null, () => {});
 *
 * // $.ajax 形态
 * HttpClient.ajax({url: 'admin/sysUser/page', method: 'GET', params, success: render});
 *
 * // 下载
 * HttpClient.download({url: 'admin/sysUser/export', method: 'POST', data: {deptId: 1},
 *     onDownloadProgress: ({loaded, total}) => console.log(loaded, total)});
 * ```
 */
/** 统一的错误数据格式：业务失败与网络异常都包装成此结构传给 error 回调 */
export interface AjaxError {
    code?: number | string;
    message: string;
}

interface AjaxSettings<T = unknown> {
    url: string;
    method?: 'GET' | 'POST';
    params?: unknown;
    data?: unknown;
    headers?: AxiosRequestConfig['headers'];
    success?: (data: T) => void;
    error?: (e: AjaxError) => void;
}

interface DownloadSettings {
    url: string;
    method?: 'GET' | 'POST';
    params?: unknown;
    data?: unknown;
    fileName?: string;
    onDownloadProgress?: (progress: {loaded: number; total: number}) => void;
    success?: (blob: Blob) => void;
    error?: (e: AjaxError) => void;
}

const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_SERVER_SERVLET_CONTEXT_PATH,
    withCredentials: true,
    headers: {'Content-Type': 'application/json'},
    paramsSerializer: (params: Record<string, unknown>) => qs.stringify(params, {indices: false})
});

export class HttpClient {
    static ajax<T = unknown>(settings: AjaxSettings<T>): void {
        const {url, method = 'GET', params, data, headers, success, error} = settings;
        HttpClient.coreRequest<T>({
            url, method, params, data, headers, success, error
        });
    }

    static get<T = unknown>(url: string, params: unknown = null, success?: (data: T) => void, error?: (e: AjaxError) => void): void {
        HttpClient.ajax<T>({url, method: 'GET', params, success, error});
    }

    static post<T = unknown>(url: string, data: unknown = null, params: unknown = null, success?: (data: T) => void, error?: (e: AjaxError) => void): void {
        HttpClient.ajax<T>({url, method: 'POST', data, params, success, error});
    }

    static postForm<T = unknown>(url: string, data: unknown, success?: (data: T) => void, error?: (e: AjaxError) => void): void {
        HttpClient.ajax<T>({
            url,
            method: 'POST',
            data: qs.stringify(data),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            success,
            error
        });
    }

    /**
     * 下载文件（responseType: blob）。
     * 成功即触发浏览器保存；JSON 错误响应自动识别为失败；文件名优先 settings.fileName，
     * 否则从 Content-Disposition 解析。
     */
    static download(settings: DownloadSettings): void {
        const {url, method = 'GET', params, data, fileName, onDownloadProgress, success, error} = settings;
        const hasError = typeof error === 'function';

        const finishError = (e: unknown) => {
            const err = HttpClient.toAjaxError(e);
            if (hasError) {
                error!(err);
            } else {
                console.warn(`[HttpClient] 下载失败: ${url}`, err.message);
                messageApi.error(err.message);
            }
        };

        axiosInstance({
            url: HttpClient.prefixUrl(url),
            method,
            params,
            data,
            responseType: 'blob',
            onDownloadProgress: (progressEvent: AxiosProgressEvent) => {
                if (onDownloadProgress) {
                    onDownloadProgress({loaded: progressEvent.loaded, total: progressEvent.total});
                }
            }
        }).then((response) => {
            const blob: Blob = response.data;

            if (blob.type === 'application/json') {
                const reader = new FileReader();
                reader.onerror = () => finishError({message: '读取下载数据失败'});
                reader.onload = () => {
                    try {
                        const rs = JSON.parse(reader.result as string);
                        if (rs.code === 401) {
                            EventBus.emit('loginExpired');
                        }
                        finishError({code: rs.code, message: rs.message || '下载失败'});
                    } catch {
                        finishError({message: '解析错误响应失败'});
                    }
                };
                reader.readAsText(blob, 'utf-8');
                return;
            }

            let filename = fileName;
            if (!filename) {
                const contentDisposition = response.headers['content-disposition'] || response.headers['Content-Disposition'];
                if (contentDisposition) {
                    const match = /filename\*?=(?:['"]?)(?:UTF-8''|)(.+?)(?:['"]?$|;)/i.exec(contentDisposition);
                    let parsedName = match?.[1] ? match[1].trim() : 'download.file';
                    try {
                        parsedName = decodeURIComponent(parsedName.replace(/"/g, ''));
                    } catch {
                        parsedName = parsedName.replace(/"/g, '');
                    }
                    filename = parsedName;
                }
            }

            const objectUrl = window.URL.createObjectURL(new Blob([blob]));
            const link = document.createElement('a');
            link.style.display = 'none';
            link.href = objectUrl;
            link.download = filename || 'download.file';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(objectUrl);
            success?.(blob);
        }).catch((e: unknown) => {
            if (axios.isAxiosError(e) && e.response?.status === 401) {
                EventBus.emit('loginExpired');
                const err: AjaxError = {code: 401, message: '登录过期'};
                if (hasError) {
                    error!(err);
                } else {
                    messageApi.error('登录过期');
                }
                return;
            }
            finishError(e);
        });
    }

    /**
     * 从 axios 异常/Error/AjaxError 中提取人类可读错误消息。
     * error 回调收到的已是 AjaxError，直接取 e.message 即可；此方法供传原始异常的场景兜底。
     */
    static errToMsg(e: unknown, defaultMsg = '操作失败'): string {
        if (axios.isAxiosError(e)) {
            const responseData = e.response?.data;
            const status = e.response?.status;
            if (status === 504) return '504 请求后端服务失败';
            if (responseData?.message) return responseData.message;
            if (e.message) return e.message;
            return defaultMsg;
        }
        if (e instanceof Error) return e.message || defaultMsg;
        if (e && typeof e === 'object' && (e as {message?: unknown}).message) return (e as {message: string}).message;
        return defaultMsg;
    }

    /** 把任意异常（AxiosError / Error / AjaxError / 业务消息）统一包装成 AjaxError */
    private static toAjaxError(e: unknown, defaultMsg = '操作失败'): AjaxError {
        if (e && typeof e === 'object' && typeof (e as {message?: unknown}).message === 'string') {
            const ajaxErr = e as {code?: number | string; message: string};
            return {code: ajaxErr.code, message: ajaxErr.message};
        }
        return {message: HttpClient.errToMsg(e, defaultMsg)};
    }

    private static prefixUrl(url: string): string {
        return url.startsWith('admin') ? '/' + url : url;
    }

    private static coreRequest<T = unknown>(settings: AjaxSettings<T>): void {
        const {url, method, params, data, headers, success, error} = settings;
        const config: AxiosRequestConfig = {url: HttpClient.prefixUrl(url), method, params, data, headers};

        // FormData 请求交由浏览器自动生成带 boundary 的 multipart Content-Type
        if (typeof FormData !== 'undefined' && data instanceof FormData) {
            config.headers = {...config.headers, 'Content-Type': undefined};
        }

        const hasError = typeof error === 'function';

        const fail = (e: unknown) => {
            if (axios.isAxiosError(e) && e.response?.status === 401) {
                EventBus.emit('loginExpired');
                const err: AjaxError = {code: 401, message: '登录过期'};
                if (hasError) {
                    error!(err);
                } else {
                    messageApi.error('登录过期');
                }
                return;
            }
            const err = HttpClient.toAjaxError(e);
            if (hasError) {
                error!(err);
            } else {
                console.warn(`[HttpClient] 请求失败: ${url}`, err.message);
                messageApi.error(err.message);
            }
        };

        axiosInstance(config).then((response) => {
            const body = response.data;
            const {success: ok, message, data: result} = body;
            if (ok == undefined) {
                success?.(response as unknown as T);
                return;
            }
            if (!ok) {
                if (body.code === 401) {
                    EventBus.emit('loginExpired');
                }
                fail({code: body.code, message: message || '操作失败'});
                return;
            }
            success?.(result);
        }).catch(fail);
    }
}
