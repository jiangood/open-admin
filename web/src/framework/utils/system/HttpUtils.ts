import axios, { AxiosRequestConfig, AxiosResponse, Method } from "axios";
import { message as messageUtil, Modal } from "antd";
import qs from 'qs';
import { MsgBox } from "../components"; // 假设路径正确
import { PageUtil } from "./page"; // 假设路径正确

/**
 * @description 请求配置接口，继承AxiosConfig并添加自定义选项
 */
interface RequestOptions extends AxiosRequestConfig {
    // 是否在请求成功后只返回body.data（默认：true）
    transformData?: boolean;
    // 是否自动显示成功/失败消息（默认：true）
    showMessage?: boolean;
    // 是否显示加载动画（默认：false）
    showLoading?: boolean;
}

/**
 * @description HTTP请求工具类
 */
export class HttpUtils {

    // 默认配置
    private static readonly DEFAULT_OPTIONS: Partial<RequestOptions> = {
        transformData: true,
        showMessage: true,
        showLoading: false,
        withCredentials: true,
        headers: {
            'Content-Type': 'application/json'
        },
        paramsSerializer: (params) => {
            return qs.stringify(params, { indices: false });
        }
    };

    /**
     * 核心请求函数，处理请求发送、加载状态、响应解析和错误捕获
     * @param config AxiosRequestConfig & RequestOptions
     * @returns Promise<any>
     */
    private static async coreRequest(config: RequestOptions): Promise<any> {

        // 合并配置
        const finalConfig: RequestOptions = { ...HttpUtils.DEFAULT_OPTIONS, ...config };

        const { url, showLoading, transformData, showMessage, ...axiosConfig } = finalConfig;

        // URL处理 (处理可能存在的 '//' 开头，使其兼容相对路径)
        if (url && url.startsWith("//")) {
            axiosConfig.url = url.substring(1);
        } else {
            axiosConfig.url = url;
        }

        let hideLoading: (() => void) | null = null;
        if (showLoading) {
            hideLoading = messageUtil.loading('处理中...', 0);
        }

        try {
            const response: AxiosResponse = await axios(axiosConfig);
            const body = response.data;
            // 假设后端响应结构为 { success: boolean | null, message: string, data: any, code: number }
            const { success, message, data } = body;

            // 自动消息提示
            if (showMessage && success !== null && message) {
                if (success) {
                    messageUtil.success(message);
                } else {
                    messageUtil.error(message);
                }
            }

            // 不转换数据，直接返回完整的响应
            if (!transformData) {
                return response;
            }

            // 后端返回结果没有success标志，则原样返回
            if (success == null) {
                return body;
            }

            // 成功则返回data，失败则抛出错误信息
            if (success) {
                return data;
            } else {
                // 抛出后端定义的错误信息
                throw new Error(message || '操作失败');
            }

        } catch (e: unknown) {
            // 关闭加载动画
            if (hideLoading) {
                hideLoading();
            }

            // 统一异常处理
            let msg = '操作失败';

            if (axios.isAxiosError(e)) {
                const status = e.response?.status;
                const responseData = e.response?.data;

                if (status === 401) {
                    // 登录过期处理
                    MsgBox.confirm('登录已过期，请重新登录').then(() => {
                        PageUtil.redirectToLogin();
                    });
                    // 阻止后续的错误提示，返回一个特殊 Promise.reject
                    return Promise.reject('登录过期');
                } else if (status === 504) {
                    msg = '504 请求后端服务失败';
                } else if (responseData && responseData.message) {
                    msg = responseData.message;
                } else if (e.message) {
                    msg = e.message;
                }
            } else if (e instanceof Error) {
                // 可能是后端success=false抛出的自定义错误
                msg = e.message;
            }

            if (showMessage && msg !== '登录过期') {
                messageUtil.error(msg);
            }

            // 将原始错误或处理后的错误信息向外抛出
            return Promise.reject(e);

        } finally {
            if (hideLoading) {
                hideLoading();
            }
        }
    }


    /**
     * 处理文件下载后的blob流
     * @param res AxiosResponse 文件下载响应
     */
    private static handleDownloadBlob(res: AxiosResponse): Promise<void> {
        return new Promise((resolve, reject) => {
            const { data: blob, headers } = res;

            // 1. 检查是否为错误JSON
            if (blob.type === 'application/json') {
                const reader = new FileReader();
                reader.readAsText(blob, 'utf-8');
                reader.onload = function () {
                    try {
                        let rs = JSON.parse(reader.result as string);
                        Modal.error({
                            title: '下载文件失败',
                            content: rs.message || '不支持下载'
                        });
                        reject(new Error(rs.message || '下载错误'));
                    } catch (e) {
                        Modal.error({ title: '下载文件失败', content: '解析错误响应失败' });
                        reject(e);
                    }
                };
                return;
            }

            // 2. 获取文件名称
            const contentDisposition = headers['content-disposition'] || headers['Content-Disposition'];
            if (!contentDisposition) {
                Modal.error({ title: '获取文件名称失败', content: "缺少Content-Disposition响应头" });
                reject(new Error("缺少Content-Disposition响应头"));
                return;
            }

            const match = /filename\*?=(?:['"]?)(?:UTF-8''|)(.+?)(?:['"]?$|;)/i.exec(contentDisposition);
            let filename = match && match[1] ? match[1].trim() : 'download.file';

            try {
                // 尝试进行URI解码
                filename = decodeURIComponent(filename.replace(/"/g, ''));
            } catch (e) {
                // 如果解码失败，保持原样
                filename = filename.replace(/"/g, '');
            }

            // 3. 开始下载
            const url = window.URL.createObjectURL(new Blob([blob]));
            const link = document.createElement('a');
            link.style.display = 'none';

            link.href = url;
            link.download = filename;

            document.body.appendChild(link);
            link.click();

            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            resolve();
        });
    }


    // --- 暴露给外部调用的公共静态方法 ---

    /**
     * 发送GET请求
     * @param url 请求地址
     * @param params URL参数
     * @param options 自定义配置 (showMessage, transformData等)
     * @returns Promise<any> 后端返回的 data 字段
     */
    public static get(url: string, params: any = null, options: Partial<RequestOptions> = {}): Promise<any> {
        return HttpUtils.coreRequest({
            ...options,
            url,
            method: 'GET',
            params,
        });
    }

    /**
     * 发送POST请求 (Content-Type: application/json)
     * @param url 请求地址
     * @param data 请求体数据
     * @param params URL参数
     * @param options 自定义配置
     * @returns Promise<any> 后端返回的 data 字段
     */
    public static post(url: string, data: any = null, params: any = null, options: Partial<RequestOptions> = {}): Promise<any> {
        return HttpUtils.coreRequest({
            ...options,
            url,
            method: 'POST',
            data,
            params,
        });
    }

    /**
     * 发送POST表单请求 (Content-Type: application/x-www-form-urlencoded)
     * @param url 请求地址
     * @param data 表单数据
     * @param options 自定义配置
     * @returns Promise<any> 后端返回的 data 字段
     */
    public static postForm(url: string, data: any, options: Partial<RequestOptions> = {}): Promise<any> {
        return HttpUtils.coreRequest({
            ...options,
            url,
            method: 'POST',
            data: qs.stringify(data), // 转换为表单格式
            headers: {
                ...HttpUtils.DEFAULT_OPTIONS.headers,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
        });
    }

    /**
     * 下载文件
     * @param url 请求地址
     * @param data 请求体数据 (POST时使用)
     * @param params URL参数
     * @param method 请求方法，默认为GET
     * @param options 自定义配置
     * @returns Promise<void>
     */
    public static async downloadFile(url: string, data: any = null, params: any = null, method: Method = 'GET', options: Partial<RequestOptions> = {}): Promise<void> {

        // 下载请求默认设置：不转换数据，响应类型为 blob
        const downloadConfig: RequestOptions = {
            transformData: false,
            showMessage: true,
            responseType: 'blob', // 关键设置
            ...options,
            url,
            method,
            params,
            data,
        };

        try {
            // coreRequest返回的是完整的 AxiosResponse，因此需要类型断言
            const response = await HttpUtils.coreRequest(downloadConfig) as AxiosResponse;
            await HttpUtils.handleDownloadBlob(response);
        } catch (error) {
            // coreRequest已经处理了401、504和通用的错误提示
            return Promise.reject(error);
        }
    }

    // --- 兼容/弃用方法 ---

    /**
     * @deprecated 请使用 HttpUtils.get 代替
     */
    public static pageData(url: string, params: any): Promise<any> {
        console.warn("pageData is deprecated, please use HttpUtils.get instead.");
        return HttpUtils.get(url, params);
    }

    /**
     * @deprecated 请使用 HttpUtils.downloadFile 代替
     */
    public static downloadFileGet(url: string, params: any, headers?: any): Promise<void> {
        console.warn("downloadFileGet is deprecated, please use HttpUtils.downloadFile instead.");
        return HttpUtils.downloadFile(url, null, params, 'GET', { headers });
    }

    /**
     * @deprecated 请使用 HttpUtils.downloadFile 代替
     */
    public static downloadFilePost(url: string, data: any, params: any, headers?: any): Promise<void> {
        console.warn("downloadFilePost is deprecated, please use HttpUtils.downloadFile instead.");
        return HttpUtils.downloadFile(url, data, params, 'POST', { headers });
    }
}
