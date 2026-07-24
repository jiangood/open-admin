import axios from "axios";
import qs from 'qs';
import {PageUtils} from "./PageUtils";
import {MessageUtils} from "../MessageUtils";

const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_SERVLET_CONTEXT || '',
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

}
