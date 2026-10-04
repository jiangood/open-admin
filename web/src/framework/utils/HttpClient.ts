import axios from "axios";
import type {AxiosProgressEvent, AxiosRequestConfig} from "axios";
import qs from 'qs';
import {message as messageApi} from "antd";
import {EventBus} from "./EventBus";

/**
 * 单一 Promise 风格 HTTP 工具类（所有请求方法均返回 Promise，无 success/error 回调）
 *
 * 核心规则：
 * - 请求方法返回 Promise<AjaxBody<T>>：成功 resolve 整个 AjaxResult 响应体，失败 reject(AjaxError {code, message})
 * - 成功 resolve 的是原始响应体 {success, code, data, message, traceId, ...}（extData 等附加字段在顶层），
 *   业务取数据用 rs.data，取提示文案用 rs.message
 * - 成功且后端返回了 message 时自动 message.success(body.message)，传 {toastSuccess: false} 可关闭
 * - 失败默认自动 console.warn + message.error，并 reject(AjaxError)；传 {toastError: false} 可完全静默（仅 reject）
 * - 返回值被忽略也安全：内部已对返回的 Promise 挂 no-op catch，不会产生 unhandled rejection
 * - reject 的 AjaxError {code, message}：业务失败 code 为后端业务码，网络异常 code 为 HTTP 状态码，message 始终为人类可读提示
 * - HTTP 401 或业务 code=401 均广播 loginExpired（全局关注点），弹错仍按 toastError 决定
 *
 * 用法：
 * ```ts
 * // 提交：成功自动弹后端 message（如"更新成功"），失败自动弹错并 reject（可用 await 感知结果，如 FormModal 提交失败保持弹窗）
 * await HttpClient.post('admin/sysUser/create', values);
 *
 * // 取返回数据与后端提示文案
 * const {data, message} = await HttpClient.post('admin/sysUser/create', values);
 *
 * // 成功自控提示（关闭自动弹后由业务决定）
 * await HttpClient.post('admin/sysUser/delete', {id}, null, {toastSuccess: false});
 * messageApi.success('删除成功');
 * reload();
 *
 * // 失败由调用方接管，不弹错（静默探测）
 * HttpClient.get('admin/public/site-info', null, {toastError: false})
 *     .then(({data}) => render(data))
 *     .catch((e) => myHandle(e));
 *
 * // $.ajax 形态
 * HttpClient.ajax({url: 'admin/sysUser/page', method: 'GET', params}).then((rs) => render(rs.data));
 *
 * // 下载（返回 Promise<Blob>，成功已触发浏览器保存）
 * HttpClient.download({url: 'admin/sysUser/export', method: 'POST', data: {deptId: 1},
 *     onDownloadProgress: ({loaded, total}) => console.log(loaded, total)})
 *     .catch(() => {});
 * ```
 */
/** 统一的错误数据格式：业务失败与网络异常都包装成此结构传给 reject 的 error */
export interface AjaxError {
    code?: number | string;
    message: string;
}

/** 后端统一响应结构 AjaxResult；putExtData 附加字段在顶层，data 为业务数据 */
export interface AjaxBody<T = unknown> {
    success: boolean;
    code: number;
    data: T;
    message?: string;
    traceId?: string;
    [key: string]: unknown;
}

/** 请求选项：headers 自定义请求头；toastError=false 时失败不自动弹错（完全静默，仅 reject）；toastSuccess=false 时成功不自动弹后端 message */
export interface RequestOptions {
    headers?: AxiosRequestConfig['headers'];
    toastError?: boolean;
    toastSuccess?: boolean;
}

interface AjaxSettings {
    url: string;
    method?: 'GET' | 'POST';
    params?: unknown;
    data?: unknown;
    headers?: AxiosRequestConfig['headers'];
    toastError?: boolean;
    toastSuccess?: boolean;
}

interface DownloadSettings {
    url: string;
    method?: 'GET' | 'POST';
    params?: unknown;
    data?: unknown;
    fileName?: string;
    onDownloadProgress?: (progress: {loaded: number; total: number}) => void;
    toastError?: boolean;
}

const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_SERVER_SERVLET_CONTEXT_PATH,
    withCredentials: true,
    headers: {'Content-Type': 'application/json'},
    paramsSerializer: (params: Record<string, unknown>) => qs.stringify(params, {indices: false})
});

export class HttpClient {
    static ajax<T = unknown>(settings: AjaxSettings): Promise<AjaxBody<T>> {
        const {url, method = 'GET', params, data, headers, toastError, toastSuccess} = settings;
        return HttpClient.coreRequest<T>({
            url, method, params, data, headers, toastError, toastSuccess
        });
    }

    static get<T = unknown>(url: string, params: unknown = null, opts?: RequestOptions): Promise<AjaxBody<T>> {
        return HttpClient.ajax<T>({url, method: 'GET', params, ...opts});
    }

    static post<T = unknown>(url: string, data: unknown = null, params: unknown = null, opts?: RequestOptions): Promise<AjaxBody<T>> {
        return HttpClient.ajax<T>({url, method: 'POST', data, params, ...opts});
    }

    static postForm<T = unknown>(url: string, data: unknown, opts?: RequestOptions): Promise<AjaxBody<T>> {
        return HttpClient.ajax<T>({
            url,
            method: 'POST',
            data: qs.stringify(data),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            ...opts
        });
    }

    /**
     * 下载文件（responseType: blob），返回 Promise<Blob>。
     * 成功即触发浏览器保存并 resolve(blob)；JSON 错误响应自动识别为失败并 reject；
     * 文件名优先 settings.fileName，否则从 Content-Disposition 解析。
     * 失败默认自动弹错，传 {toastError: false} 可静默（仅 reject）。
     */
    static download(settings: DownloadSettings): Promise<Blob> {
        const {url, method = 'GET', params, data, fileName, onDownloadProgress, toastError = true} = settings;

        return new Promise<Blob>((resolve, reject) => {
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
                    reader.onerror = () => HttpClient.fail(url, toastError, reject, {message: '读取下载数据失败'}, '下载失败');
                    reader.onload = () => {
                        try {
                            const rs = JSON.parse(reader.result as string);
                            if (rs.code === 401) {
                                EventBus.emit('loginExpired');
                            }
                            HttpClient.fail(url, toastError, reject, {code: rs.code, message: rs.message || '下载失败'}, '下载失败');
                        } catch {
                            HttpClient.fail(url, toastError, reject, {message: '解析错误响应失败'}, '下载失败');
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
                            parsedName = decodeURIComponent(parsedName.replaceAll('"', ''));
                        } catch {
                            parsedName = parsedName.replaceAll('"', '');
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
                link.remove();
                window.URL.revokeObjectURL(objectUrl);
                resolve(blob);
            }).catch((e: unknown) => {
                HttpClient.fail(url, toastError, reject, e, '下载失败');
            });
        });
    }

    /**
     * 从 axios 异常/Error/AjaxError 中提取人类可读错误消息。
     * catch 收到的已是 AjaxError，直接取 e.message 即可；此方法供传原始异常的场景兜底。
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

    private static coreRequest<T = unknown>(settings: AjaxSettings): Promise<AjaxBody<T>> {
        const {url, method, params, data, headers, toastError = true, toastSuccess = true} = settings;
        const config: AxiosRequestConfig = {url: HttpClient.prefixUrl(url), method, params, data, headers};

        // FormData 请求交由浏览器自动生成带 boundary 的 multipart Content-Type
        if (typeof FormData !== 'undefined' && data instanceof FormData) {
            config.headers = {...config.headers, 'Content-Type': undefined};
        }

        const promise = new Promise<AjaxBody<T>>((resolve, reject) => {
            axiosInstance(config).then((response) => {
                const body = response.data as AjaxBody<T>;
                const {success: ok, message} = body;
                if (ok == undefined) {
                    resolve(body);
                    return;
                }
                if (!ok) {
                    if (body.code === 401) {
                        EventBus.emit('loginExpired');
                    }
                    HttpClient.fail(url, toastError, reject, {code: body.code, message: message || '操作失败'});
                    return;
                }
                if (toastSuccess && message) {
                    messageApi.success(message);
                }
                resolve(body);
            }).catch((e: unknown) => {
                HttpClient.fail(url, toastError, reject, e);
            });
        });

        // 调用方忽略返回值时兜底，避免 unhandled rejection；await 的调用方仍能正常收到 reject
        promise.catch(() => {});
        return promise;
    }

    /** 统一的失败处理：HTTP 401 广播 loginExpired 并弹"登录过期"，其余提取可读消息后按 toastError 决定是否弹错，最终 reject(AjaxError) */
    private static fail(url: string, toastError: boolean, reject: (reason?: unknown) => void, e: unknown, label = '请求失败'): void {
        if (axios.isAxiosError(e) && e.response?.status === 401) {
            EventBus.emit('loginExpired');
            const err: AjaxError = {code: 401, message: '登录过期'};
            if (toastError) {
                messageApi.error('登录过期');
            }
            reject(err);
            return;
        }
        const err = HttpClient.toAjaxError(e);
        if (toastError) {
            console.warn(`[HttpClient] ${label}: ${url}`, err.message);
            messageApi.error(err.message);
        }
        reject(err);
    }
}