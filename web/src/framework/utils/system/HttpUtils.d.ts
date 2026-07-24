import {AxiosRequestConfig} from "axios";

interface RequestOptions extends AxiosRequestConfig {
    showError?: boolean;
}

export class HttpUtils {
    static coreRequest<T = any>(config: RequestOptions, transformData?: boolean): Promise<T>;
    static get<T = any>(url: string, params?: any, options?: Partial<RequestOptions>): Promise<T>;
    static post<T = any>(url: string, data?: any, params?: any, options?: Partial<RequestOptions>): Promise<T>;
    static postForm<T = any>(url: string, data: any, options?: Partial<RequestOptions>): Promise<T>;
}
