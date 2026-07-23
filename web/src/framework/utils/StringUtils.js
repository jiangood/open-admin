export class StringUtils {

    static ISO_SPLITTER = "/";

    static removePrefix(str, ch) {
        if (str != null && str.startsWith(ch)) {
            return str.substring(ch.length);
        }
        return str ?? '';
    }

    static removeSuffix(str, ch) {
        if (str != null && str.endsWith(ch)) {
            return str.substring(0, str.length - ch.length);
        }
        return str ?? '';
    }

    static random(length) {
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        let result = '';
        for (let i = 0; i < length; i++) {
            result += characters.charAt(Math.floor(Math.random() * characters.length));
        }
        return result;
    }

    static contains(str, subStr) {
        if (!str) return false;
        return str.includes(subStr);
    }

    static subAfter(source, str) {
        if (source == null) return '';
        const index = source.indexOf(str);
        return index === -1 ? source : source.substring(index + str.length);
    }

    static subAfterLast(source, str) {
        if (source == null) return '';
        const index = source.lastIndexOf(str);
        return index === -1 ? source : source.substring(index + str.length);
    }

    static subBefore(str, sub) {
        if (str == null) return "";
        const index = str.indexOf(sub);
        return index === -1 ? str : str.substring(0, index);
    }

    static pad(input, totalLen, padChar = '0') {
        if (input == null) return padChar.repeat(totalLen);
        let str = String(input);
        const charsNeeded = totalLen - str.length;
        if (charsNeeded > 0) {
            str = padChar.repeat(charsNeeded) + str;
        }
        return str;
    }

    static getWidth(str) {
        if (str == null || str.length === 0) return 0;
        return str.split('').reduce((pre, cur) => {
            const charCode = cur.charCodeAt(0);
            return pre + (charCode >= 0 && charCode <= 128 ? 1 : 2);
        }, 0);
    }

    static cutByWidth(str, maxWidth) {
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

    static ellipsis(str, len, suffix = '...') {
        if (str == null) return '';
        if (!StringUtils.isString(str)) return '';
        if (str.length * 2 < len) return str;
        const fullLength = StringUtils.getWidth(str);
        if (fullLength <= len) return str;
        return StringUtils.cutByWidth(str, len) + suffix;
    }

    static isString(value) {
        return typeof value === 'string';
    }

    static split(str, sp) {
        if (str == null || str.length === 0) return [];
        if (Array.isArray(str)) return str;
        return str.split(sp);
    }

    static join(arr, sp) {
        if (arr == null || !Array.isArray(arr)) {
            return '';
        }
        return arr.join(sp);
    }
}
