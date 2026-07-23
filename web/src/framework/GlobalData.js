import {StorageUtils} from '../StorageUtils';

const SITE_INFO_KEY = "siteInfo";
const LOGIN_INFO_KEY = "loginInfo";
const DICT_INFO_KEY = "dictInfo";

export class GlobalData {
    static setSiteInfo(data) {
        StorageUtils.set(SITE_INFO_KEY, data);
    }

    static getSiteInfo() {
        return StorageUtils.get(SITE_INFO_KEY) || {};
    }

    static setLoginInfo(data) {
        StorageUtils.set(LOGIN_INFO_KEY, data);
    }

    static getLoginInfo() {
        return StorageUtils.get(LOGIN_INFO_KEY) || {};
    }

    static setDictInfo(data) {
        StorageUtils.set(DICT_INFO_KEY, data);
    }

    static getDictInfo() {
        return StorageUtils.get(DICT_INFO_KEY);
    }
}

export { SITE_INFO_KEY, LOGIN_INFO_KEY, DICT_INFO_KEY };
