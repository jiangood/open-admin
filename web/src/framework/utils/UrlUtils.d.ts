export class UrlUtils {
    static getParams(url?: string | null): Record<string, string>;
    static setParam(url: string, key: string, value: string | number | boolean | null | undefined): string;
    static getWebsocketBaseUrl(): string;
    static contextPath(path: string): string;
}
