export class GlobalData {
    static setSiteInfo(data: object): void;
    static getSiteInfo(): object;
    static setLoginInfo(data: object): void;
    static getLoginInfo(): object;
    static setDictInfo(data: object | null): void;
    static getDictInfo(): object | null;
    static setSiteArticles(data: object): void;
    static getSiteArticles(): object;
}
