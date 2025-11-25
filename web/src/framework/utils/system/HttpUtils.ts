import axios, {AxiosRequestConfig, AxiosResponse, Method} from "axios";
import {message as messageUtil, Modal} from "antd";
import qs from 'qs';
import {PageUtils} from "./PageUtils";
import {MsgBox} from "../../components"; // 假设路径正确


const axiosInstance = axios.create({
    withCredentials: true,
    headers:{
        'Content-Type': 'application/json'
    },
    // 解决get请求时，数组参数的问题
    paramsSerializer: (params) => {
        return qs.stringify(params, {indices: false});
    }
})



/**
 * @description HTTP请求工具类
 */
export class HttpUtils {



    /**
     * 核心请求函数，处理请求发送、加载状态、响应解析和错误捕获
     * @param config AxiosRequestConfig & RequestOptions
     * @param transformData 是否返回最总的data字段
     * @returns Promise<any>
     */
    private static async coreRequest(config: AxiosRequestConfig, transformData:boolean=true): Promise<any> {
        const url = config.url;
        config.url = url.startsWith('admin') ? '/' + url : url;

        try {
            const response: AxiosResponse = await axios(config);
            const body = response.data;
            // 假设后端响应结构为 { success: boolean | null, message: string, data: any, code: number }
            let {success, message, data} = body;
            if (success == undefined) { // 如果没有success字段，说明非标准接口
                return response;
            }
            if (!success) {
                messageUtil.success(message || '操作失败');
                return
            }

            // 自动消息提示
            if (message) {
                messageUtil.success(message);
            }

            return transformData ? data : response;
        } catch (e: unknown) {
            // 统一异常处理
            let msg = '操作失败';

            if (axios.isAxiosError(e)) {
                const status = e.response?.status;
                const responseData = e.response?.data;

                if (status === 401) {
                    // 登录过期处理
                    MsgBox.confirm('登录已过期，请重新登录').then(() => {
                        PageUtils.redirectToLogin();
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

            messageUtil.error(msg);

            // 将原始错误或处理后的错误信息向外抛出
             throw e;

        }
    }


    /**
     * 处理文件下载后的blob流
     * @param res AxiosResponse 文件下载响应
     */
    private static handleDownloadBlob(res: AxiosResponse): Promise<void> {
        return new Promise((resolve, reject) => {
            const {data: blob, headers} = res;

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
                        Modal.error({title: '下载文件失败', content: '解析错误响应失败'});
                        reject(e);
                    }
                };
                return;
            }

            // 2. 获取文件名称
            const contentDisposition = headers['content-disposition'] || headers['Content-Disposition'];
            if (!contentDisposition) {
                Modal.error({title: '获取文件名称失败', content: "缺少Content-Disposition响应头"});
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



    /**
     * 发送GET请求
     * @param url 请求地址
     * @param params URL参数
     * @param options 自定义配置 (transformData等)
     * @returns Promise<any> 后端返回的 data 字段
     */
    public static get(url: string, params: any = null, options: Partial<AxiosRequestConfig> = {}): Promise<any> {
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
    public static postForm(url: string, data: any, options: Partial<AxiosRequestConfig> = {}): Promise<any> {
        return HttpUtils.coreRequest({
            ...options,
            url,
            method: 'POST',
            data: qs.stringify(data), // 转换为表单格式
            headers: {
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
    public static async downloadFile(url: string, data: any = null, params: any = null, method: Method = 'GET', options: Partial<AxiosRequestConfig> = {}): Promise<void> {

        // 下载请求默认设置：不转换数据，响应类型为 blob
        const downloadConfig: AxiosRequestConfig = {
            responseType: 'blob', // 关键设置
            ...options,
            url,
            method,
            params,
            data,
        };

        try {
            // coreRequest返回的是完整的 AxiosResponse，因此需要类型断言
            const response = await HttpUtils.coreRequest(downloadConfig,false) as AxiosResponse;
            await HttpUtils.handleDownloadBlob(response);
        } catch (error) {
            // coreRequest已经处理了401、504和通用的错误提示
            return Promise.reject(error);
        }
    }


}
