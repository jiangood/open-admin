export class StringUtils {
    static readonly ISO_SPLITTER: string;
    static removePrefix(str: string | null | undefined, ch: string): string;
    static removeSuffix(str: string | null | undefined, ch: string): string;
    static random(length: number): string;
    static contains(str: string | null | undefined, subStr: string): boolean;
    static subAfter(source: string | null | undefined, str: string): string;
    static subAfterLast(source: string | null | undefined, str: string): string;
    static subBefore(str: string | null | undefined, sub: string): string;
    static pad(input: string | number | null | undefined, totalLen: number, padChar?: string): string;
    static getWidth(str: string | null | undefined): number;
    static cutByWidth(str: string, maxWidth: number): string;
    static ellipsis(str: string | null | undefined, len: number, suffix?: string): string;
    static isString(value: any): value is string;
    static split(str: any, sp: string): string[];
    static join(arr: any, sp: string): string;
}
