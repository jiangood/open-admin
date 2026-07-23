import axios from "axios";
import {Modal} from "antd";
import qs from 'qs';
import {PageUtils} from "./PageUtils";
import {MessageUtils} from "../MessageUtils";

const axiosInstance = axios.create({
    baseURL: (typeof SERVLET_CONTEXT !== 'undefined' && SERVLET_CONTEXT) || '',
    withCredentials: true,
    headers: { 'Content-Type': 'application/json' },
    paramsSerializer: (params) => qs.stringify(params, {indices: false})
});

export class HttpUtils {
    static coreRequest(config, transformData = true) {
        const url = config.url;
        config.url = url.startsWith('admin') ? '/' + url : url;

        return new Promise((resolve, reject) => {
            axiosInstance(config).then((response) => {
                const body = response.data;
                let {success, message, data} = body;
                if (success == undefined) {
                    resolve(response);
                    return;
                }
                if (!success) {
                    console.error(`[HttpUtils] 请求失败: ${url}`, {code: body.code, message, data: body});
                    if (config.showError !== false) {
                        MessageUtils.error(message || '操作失败');
                    }
                    reject(message || '操作失败');
                    return;
                }
                if (message) {
                    MessageUtils.success(message);
                }
                resolve(transformData ? data : response);
            }).catch((e) => {
                console.error(`[HttpUtils] 请求异常: ${url}`, e);
                if (axios.isAxiosError(e) && e.response?.status === 401) {
                    MessageUtils.confirm('登录已过期，请重新登录').then(() => {
                        PageUtils.redirectToLogin();
                    });
                    reject('登录过期');
                    return;
                }
                const msg = HttpUtils.extractErrorMessage(e);
                if (config.showError !== false) {
                    MessageUtils.error(msg);
                }
                reject(msg);
            });
        });
    }

    static extractErrorMessage(e, defaultMsg = '操作失败') {
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

    static handleDownloadBlob(res) {
        return new Promise((resolve, reject) => {
            const {data: blob, headers} = res;
            if (blob.type === 'application/json') {
                const reader = new FileReader();
                reader.readAsText(blob, 'utf-8');
                reader.onload = function () {
                    try {
                        let rs = JSON.parse(reader.result);
                        Modal.error({title: '下载文件失败', content: rs.message || '不支持下载'});
                        reject(new Error(rs.message || '下载错误'));
                    } catch (e) {
                        Modal.error({title: '下载文件失败', content: '解析错误响应失败'});
                        reject(e);
                    }
                };
                return;
            }
            const contentDisposition = headers['content-disposition'] || headers['Content-Disposition'];
            if (!contentDisposition) {
                Modal.error({title: '获取文件名称失败', content: "缺少Content-Disposition响应头"});
                reject(new Error("缺少Content-Disposition响应头"));
                return;
            }
            const match = /filename\*?=(?:['"]?)(?:UTF-8''|)(.+?)(?:['"]?$|;)/i.exec(contentDisposition);
            let filename = match && match[1] ? match[1].trim() : 'download.file';
            try {
                filename = decodeURIComponent(filename.replace(/"/g, ''));
            } catch (e) {
                filename = filename.replace(/"/g, '');
            }
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

    static get(url, params = null, options = {}) {
        return HttpUtils.coreRequest({...options, url, method: 'GET', params});
    }

    static post(url, data = null, params = null, options = {}) {
        return HttpUtils.coreRequest({...options, url, method: 'POST', data, params});
    }

    static postForm(url, data, options = {}) {
        return HttpUtils.coreRequest({
            ...options,
            url,
            method: 'POST',
            data: qs.stringify(data),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
    }

    static async downloadFile(url, data = null, params = null, method = 'GET', options = {}) {
        const downloadConfig = {responseType: 'blob', ...options, url, method, params, data};
        try {
            const response = await HttpUtils.coreRequest(downloadConfig, false);
            await HttpUtils.handleDownloadBlob(response);
        } catch (error) {
            console.error('[HttpUtils] 下载文件失败:', error);
            return Promise.reject(error);
        }
    }
}
