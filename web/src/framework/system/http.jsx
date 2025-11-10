import axios from "axios";
import {message as messageUtil, Modal} from "antd";
import {SysUtil} from "./sys";
import qs from 'qs'
axios.interceptors.request.use(function (config) {
    console.log('请求参数：', config);
    return config;
}, error => {
    return Promise.reject(error);
});
axios.interceptors.response.use(function (response) {
    console.log('返回结果：', response);
    return response;
}, error => {
    console.log('返回错误：', error);
    return Promise.reject(error);
});
export const HttpUtil = {

    create() {
        return new Util();
    },


    get(url, params = null) {
        const util = new Util();
        util.enableShowMessage()
        util.enableTransformData()
        return util.get(url, params)
    },

    post(url, data = null, params = null) {
        const util = new Util();
        util.enableShowMessage()
        util.enableTransformData()

        return util.post(url, data, params)
    },


    postForm(url, data) {
        console.warn('不推荐调用本方法,设计urlEncode等，对于特殊字符可能会有问题')
        const util = new Util();
        util.enableShowMessage()
        util.enableTransformData()

        let config = {
            url,
            method: 'POST',
            data,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
        };

        return util.request(config)
    },

    /**
     * 分页请求, 为antd的ProTable
     */
    pageData(url, params) {
        const { _exportType, ...data} = params || {};

        if (_exportType) {
            return this.downloadFileGet(url, data, {'X-Export-Type': _exportType})
        }

        return this.get(url, data)
    },
    /**
     * 下载
     * @deprecated
     */
    downloadFile(url, params, method = 'GET') {
        const util = new Util();
        util.enableShowMessage()

        const get = method === 'GET';
        if (get) {
            return this.downloadFileGet(url, params)
        }

        return this.downloadFilePost(url, params)
    },

    downloadFilePost(url, data, params, headers) {
        const util = new Util();
        util.enableShowMessage()
        return util.downloadFile(url, data, params, 'POST', headers)
    },
    downloadFileGet(url, params,headers) {
        const util = new Util();
        util.enableShowMessage()
        return util.downloadFile(url, null, params, 'GET',headers)
    }
}

class Util {
    _showLoading = false

    // 请求成功结果，返回data那一层（忽略 success,code ）
    _transformData = false

    // 自动显示成功消息（message)、失败消息(modal)
    _showMessage = false

    request = config => {
        const defaultConfig = {
            withCredentials: true, // 带cookie
            baseURL: SysUtil.getServerUrl(),
            headers: {
                'Content-Type': 'application/json'
            },
            paramsSerializer: params => {
                return qs.stringify(params, { indices: false })
            }
        }


        let finalConfig = Object.assign({}, defaultConfig, config);

        const {...axiosConfig} = finalConfig

        if (axiosConfig.url.startsWith("//")) {
            axiosConfig.url = axiosConfig.url.substring(1)
        }


        return new Promise((resolve, reject) => {
            let hideLoading = null
            if (this._showLoading) {
                hideLoading = messageUtil.loading('处理中...', 0)
            }

            axios(axiosConfig).then(response => {
                const body = response.data;
                const {success, message, data, code} = body;

                if (this._showMessage && success != null && message) { // 有可能是下载
                    if (success) {
                        messageUtil.success(message)
                    } else {
                        messageUtil.error(message)
                    }
                }

                if (!this._transformData) {
                    resolve(response);
                    return;
                }


                if (success == null) { // 返回结果没有success标志，则原样
                    resolve(body);
                    return
                }

                if (success) {
                    resolve(data)
                } else {
                    reject(message)
                }


            }).catch(e => {
                if (!this._showMessage) {
                    reject(e)
                    return
                }
                let title = "请求异常";
                let msg = '操作失败';

                if(e.status  === 504){
                    msg = '504 请求后端服务失败'
                }else {
                    if (e.response && e.response.data) {
                        const rs = e.response.data
                        msg = rs.message
                    }
                    if (msg == null && e.message) {
                        msg = e.message
                    }
                }

                messageUtil.error(msg)


                reject(e)
            }).finally(() => {
                if (hideLoading) {
                    hideLoading()
                }

            })
        })

    };


    get(url, params = null) {
        let config = {
            url,
            method: 'GET',
            params,
        };

        return this.request(config)
    }

    post(url, data, params = null) {
        let config = {
            url,
            method: 'POST',
            params,
            data,
        };

        return this.request(config)
    }

    downloadFile(url, data, params, method = 'GET', headers = {}) {
        let config = {
            url,
            params,
            method,
            responseType: 'blob',
            data,
            headers
        };


        return this.request(config).then(res => {
            const {data: blob, headers} = res
            if (blob.type === 'application/json') {// 可能是错误了，否则不应该返回json
                const reader = new FileReader();
                reader.readAsText(blob, 'utf-8');
                reader.onload = function (e) {
                    let rs = JSON.parse(reader.result);
                    Modal.error({
                        title: '下载文件失败',
                        content: rs.message || '不支持下载'
                    })
                }
                return;
            }

            // 获取文件名称
            const contentDisposition = headers.get('content-disposition');
            if (contentDisposition == null) {
                Modal.error({
                    title: '获取文件名称失败',
                    content: "缺少content-disposition响应头"
                })
                return
            }


            let regExp = new RegExp('filename=(.*)');
            const result = regExp.exec(contentDisposition);
            let filename = result[1];

            filename = decodeURIComponent(filename);
            filename = filename.replaceAll('"', '');
            filename = filename.replace(/^["](.*)["]$/g, '$1')


            const url = window.URL.createObjectURL(new Blob([blob]));
            const link = document.createElement('a');
            link.style.display = 'none';

            link.href = url;
            link.download = decodeURI(filename); // 下载后文件名

            document.body.appendChild(link);
            link.click();

            document.body.removeChild(link); // 下载完成移除元素
            window.URL.revokeObjectURL(url);
        })

    }

    enableShowLoading() {
        this._showLoading = true
        return this
    }

    enableShowMessage() {
        this._showMessage = true
        return this
    }

    enableTransformData() {
        this._transformData = true
        return this
    }
}
