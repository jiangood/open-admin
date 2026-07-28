import {StorageUtils} from './utils/StorageUtils';

const SITE_INFO_KEY = "siteInfo";
const LOGIN_INFO_KEY = "loginInfo";
const DICT_INFO_KEY = "dictInfo";
const SITE_ARTICLES_KEY = "siteArticles";

export interface SiteInfo {
    title?: string;
    waterMark?: boolean;
    [key: string]: unknown;
}

export interface LoginInfo {
    name?: string;
    account?: string;
    phone?: string;
    email?: string;
    unit?: string;
    dept?: string;
    roles?: string;
    createTime?: string;
    permissions?: string[];
    [key: string]: unknown;
}

export interface DictItem {
    code: string | number;
    label: string;
    color?: string;
    typeCode?: string;
}

export interface ArticleItem {
    code: string;
    title: string;
    [key: string]: unknown;
}

export interface SiteArticles {
    HEADER_AVATAR_DROPDOWN?: ArticleItem[];
    HEADER_RIGHT?: ArticleItem[];
    HEADER_LEFT?: ArticleItem[];
    [key: string]: unknown;
}

export class GlobalData {
    static setSiteInfo(data: SiteInfo): void {
        StorageUtils.set(SITE_INFO_KEY, data);
    }

    static getSiteInfo(): SiteInfo {
        return StorageUtils.get<SiteInfo>(SITE_INFO_KEY) || {};
    }

    static setLoginInfo(data: LoginInfo): void {
        StorageUtils.set(LOGIN_INFO_KEY, data);
    }

    static getLoginInfo(): LoginInfo {
        return StorageUtils.get<LoginInfo>(LOGIN_INFO_KEY) || {};
    }

    static setDictInfo(data: DictItem[] | null): void {
        StorageUtils.set(DICT_INFO_KEY, data);
    }

    static getDictInfo(): DictItem[] | null {
        return StorageUtils.get<DictItem[]>(DICT_INFO_KEY);
    }

    static setSiteArticles(data: SiteArticles): void {
        StorageUtils.set(SITE_ARTICLES_KEY, data);
    }

    static getSiteArticles(): SiteArticles {
        return StorageUtils.get<SiteArticles>(SITE_ARTICLES_KEY) || {};
    }
}

export const ARTICLE_HEADER_LEFT = 'HEADER_LEFT';
export const ARTICLE_HEADER_RIGHT = 'HEADER_RIGHT';
export const ARTICLE_HEADER_AVATAR_DROPDOWN = 'HEADER_AVATAR_DROPDOWN';

export { SITE_INFO_KEY, LOGIN_INFO_KEY, DICT_INFO_KEY, SITE_ARTICLES_KEY };
