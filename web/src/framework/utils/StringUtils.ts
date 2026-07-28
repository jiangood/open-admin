export class StringUtils {

    static readonly ISO_SPLITTER: string = "/";

    static removePrefix(str: string | null | undefined, ch: string): string {
        if (str != null && str.startsWith(ch)) {
            return str.substring(ch.length);
        }
        return str ?? '';
    }

    static removeSuffix(str: string | null | undefined, ch: string): string {
        if (str != null && str.endsWith(ch)) {
            return str.substring(0, str.length - ch.length);
        }
        return str ?? '';
    }

    static random(length: number): string {
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        let result = '';
        for (let i = 0; i < length; i++) {
            result += characters.charAt(Math.floor(Math.random() * characters.length));
        }
        return result;
    }

    static contains(str: string | null | undefined, subStr: string): boolean {
        if (!str) return false;
        return str.includes(subStr);
    }

    static subAfter(source: string | null | undefined, str: string): string {
        if (source == null) return '';
        const index = source.indexOf(str);
        return index === -1 ? source : source.substring(index + str.length);
    }

    static subAfterLast(source: string | null | undefined, str: string): string {
        if (source == null) return '';
        const index = source.lastIndexOf(str);
        return index === -1 ? source : source.substring(index + str.length);
    }

    static subBefore(str: string | null | undefined, sub: string): string {
        if (str == null) return "";
        const index = str.indexOf(sub);
        return index === -1 ? str : str.substring(0, index);
    }

    static pad(input: string | number | null | undefined, totalLen: number, padChar = '0'): string {
        if (input == null) return padChar.repeat(totalLen);
        let str = String(input);
        const charsNeeded = totalLen - str.length;
        if (charsNeeded > 0) {
            str = padChar.repeat(charsNeeded) + str;
        }
        return str;
    }

    static getWidth(str: string | null | undefined): number {
        if (str == null || str.length === 0) return 0;
        return str.split('').reduce((pre, cur) => {
            const charCode = cur.charCodeAt(0);
            return pre + (charCode >= 0 && charCode <= 128 ? 1 : 2);
        }, 0);
    }

    static cutByWidth(str: string, maxWidth: number): string {
        let showLength = 0;
        return str.split('').reduce((pre, cur) => {
            const charCode = cur.charCodeAt(0);
            const charWidth = (charCode >= 0 && charCode <= 128) ? 1 : 2;
            if (showLength + charWidth <= maxWidth) {
                showLength += charWidth;
                return pre + cur;
            }
            return pre;
        }, '');
    }

    static ellipsis(str: string | null | undefined, len: number, suffix = '...'): string {
        if (str == null) return '';
        if (!StringUtils.isString(str)) return '';
        if (str.length * 2 < len) return str;
        const fullLength = StringUtils.getWidth(str);
        if (fullLength <= len) return str;
        return StringUtils.cutByWidth(str, len) + suffix;
    }

    static isString(value: any): value is string {
        return typeof value === 'string';
    }

    static split(str: any, sp: string): string[] {
        if (str == null || str.length === 0) return [];
        if (Array.isArray(str)) return str;
        return str.split(sp);
    }

    static join(arr: any, sp: string): string {
        if (arr == null || !Array.isArray(arr)) {
            return '';
        }
        return arr.join(sp);
    }
}
