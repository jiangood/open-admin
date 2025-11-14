import {StorageUtil, StrUtil, UrlUtil} from "@/framework";
import {history} from "umi";
import {PageUtil} from "./page";

const SITE_INFO_KEY = "siteInfo"
const LOGIN_INFO_KEY = "loginInfo"
const DICT_INFO_KEY = "dictInfo"
export const SysUtil = {

    redirectToLogin(){
        let url = PageUtil.currentUrl();
        url =encodeURIComponent(url)
        history.push('/login?redirect='+ url)
    },


    /**
     * 服务器端的地址， 以 /结尾
     * @returns {string}
     */
    getServerUrl() {
        return "/"
    },

    /**
     * 将服务url加载最前
     * @param url
     * @returns {*}
     */
    wrapServerUrl(url) {
        const serverUrl = SysUtil.getServerUrl()
        return UrlUtil.join(serverUrl, url)
    },


    getHeaders() {
        const headers = {}
        return headers;
    },

    setSiteInfo(data) {
        return StorageUtil.set(SITE_INFO_KEY, data)
    },

    getSiteInfo() {
        return StorageUtil.get(SITE_INFO_KEY) || {}
    },

    setLoginInfo(data) {
        return StorageUtil.set(LOGIN_INFO_KEY, data)
    },

    getLoginInfo() {
        return StorageUtil.get(LOGIN_INFO_KEY) || {}
    },


    setDictInfo(data) {
        return StorageUtil.set(DICT_INFO_KEY, data)
    },

    getDictInfo() {
        return StorageUtil.get(DICT_INFO_KEY)
    }
}


