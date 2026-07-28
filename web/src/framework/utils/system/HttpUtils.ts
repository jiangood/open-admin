import axios from "axios";
import type {AxiosRequestConfig} from "axios";
import qs from 'qs';
import {PageUtils} from "./PageUtils";
import {message as messageApi} from "antd";
import {EventBus} from "../EventBus";

interface RequestOptions extends AxiosRequestConfig {
    showError?: boolean;
}

const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_SERVLET_CONTEXT || '',
    withCredentials: true,
    headers: { 'Content-Type': 'application/json' },
    paramsSerializer: (params: Record<string, unknown>) => qs.stringify(params, {indices: false})
});

export class HttpUtils {
    static coreRequest<T = any>(config: RequestOptions, transformData = true): Promise<T> {
        const url = config.url as string;
        config.url = url.startsWith('admin') ? '/' + url : url;

        return new Promise<T>((resolve, reject) => {
            axiosInstance(config).then((response) => {
                const body = response.data;
                let {success, message, data} = body;
                if (success == undefined) {
                    resolve(response as unknown as T);
                    return;
                }
                if (!success) {
                    console.error(`[HttpUtils] 请求失败: ${url}`, {code: body.code, message, data: body});
                    if (config.showError !== false) {
                        messageApi.error(message || '操作失败');
                    }
                    reject(message || '操作失败');
                    return;
                }
                if (message) {
                    messageApi.success(message);
                }
                resolve((transformData ? data : response) as T);
            }).catch((e) => {
                console.error(`[HttpUtils] 请求异常: ${url}`, e);
                if (axios.isAxiosError(e) && e.response?.status === 401) {
                    EventBus.emit('loginExpired');
                    reject('登录过期');
                    return;
                }
                const msg = HttpUtils.extractErrorMessage(e);
                if (config.showError !== false) {
                    messageApi.error(msg);
                }
                reject(msg);
            });
        });
    }

    static extractErrorMessage(e: unknown, defaultMsg = '操作失败'): string {
        if (axios.isAxiosError(e)) {
            const responseData = e.response?.data;
            const status = e.response?.status;
            if (status === 504) return '504 请求后端服务失败';
            if (responseData && responseData.message) return responseData.message;
            if (e.message) return e.message;
            return defaultMsg;
        }
        if (e instanceof Error) return e.message || defaultMsg;
        return defaultMsg;
    }

    static get<T = any>(url: string, params: any = null, options: Partial<RequestOptions> = {}): Promise<T> {
        return HttpUtils.coreRequest<T>({...options, url, method: 'GET', params});
    }

    static post<T = any>(url: string, data: any = null, params: any = null, options: Partial<RequestOptions> = {}): Promise<T> {
        return HttpUtils.coreRequest<T>({...options, url, method: 'POST', data, params});
    }

    static postForm<T = any>(url: string, data: any, options: Partial<RequestOptions> = {}): Promise<T> {
        return HttpUtils.coreRequest<T>({
            ...options,
            url,
            method: 'POST',
            data: qs.stringify(data),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
    }

}
