/**
 * 字符串工具类
 */
export class StringUtils {

    /**
     * 移除字符串前缀
     * @param str 原始字符串
     * @param ch 要移除的前缀
     * @returns 移除前缀后的字符串，或原字符串
     */
    static removePrefix(str: string | null | undefined, ch: string): string | null | undefined {
        if (str != null && str.startsWith(ch)) {
            return str.substring(ch.length);
        }
        return str;
    }

    /**
     * 移除字符串后缀
     * @param str 原始字符串
     * @param ch 要移除的后缀
     * @returns 移除后缀后的字符串，或原字符串
     */
    static removeSuffix(str: string | null | undefined, ch: string): string | null | undefined {
        if (str != null && str.endsWith(ch)) {
            return str.substring(0, str.length - ch.length);
        }
        return str;
    }

    /**
     * 生成指定长度的随机字符串
     * @param length 随机字符串的长度
     * @returns 生成的随机字符串
     */
    static random(length: number): string {
        const characters: string = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        let result: string = '';
        for (let i = 0; i < length; i++) {
            result += characters.charAt(Math.floor(Math.random() * characters.length));
        }
        return result;
    }

    /**
     * 处理空值，返回 "key未定义"
     * @param key 键名
     * @returns 处理后的字符串
     */
    static nullText(key: string | null | undefined): string {
        return (key || '') + '未定义';
    }

    /**
     * 检查字符串是否包含子字符串
     * @param str 原始字符串
     * @param subStr 要检查的子字符串
     * @returns 如果包含则返回 true，否则返回 false
     */
    static contains(str: string | null | undefined, subStr: string): boolean {
        if (!str) {
            return false;
        }
        return str.includes(subStr);
    }

    /**
     * 统计子字符串在原始字符串中出现的次数
     * @param str 原始字符串
     * @param subStr 要统计的子字符串
     * @returns 出现的次数
     */
    static count(str: string | null | undefined, subStr: string): number {
        if (str == null || str.length === 0) {
            return 0;
        }
        let count: number = 0;
        let index: number = 0;

        while (true) {
            index = str.indexOf(subStr, index);
            if (index === -1) {
                break;
            }
            count++;
            index += subStr.length;
        }

        return count;
    }

    /**
     * 将字符串的首字母转换为大写
     * @param str 原始字符串
     * @returns 首字母大写后的字符串，或原值
     */
    static capitalize(str: string | null | undefined): string | null | undefined {
        if (str == null) {
            return str;
        }
        return str.charAt(0).toUpperCase() + str.slice(1);
    }

    /**
     * 颠倒字符串的顺序
     * @param str 原始字符串
     * @returns 颠倒顺序后的字符串，或原值
     */
    static reverse(str: string | null | undefined): string | null | undefined {
        if (str == null) {
            return str;
        }
        return str.split('').reverse().join('');
    }

    /**
     * 截取字符串，返回子字符串后面部分。如果不包含，则原样返回
     * @param source 原始字符串
     * @param str 分隔符
     * @returns 分隔符后面的字符串，或原字符串
     */
    static subAfter(source: string | null | undefined, str: string): string | null | undefined {
        if (source == null) {
            return source;
        }
        const index = source.indexOf(str);
        return index === -1 ? source : source.substring(index + str.length); // 修正：应为 index + str.length
    }

    /**
     * 截取字符串，返回最后一个子字符串后面的部分。如果不包含，则原样返回
     * @param source 原始字符串
     * @param str 分隔符
     * @returns 最后一个分隔符后面的字符串，或原字符串
     */
    static subAfterLast(source: string | null | undefined, str: string): string | null | undefined {
        if (source == null) {
            return source;
        }
        const index = source.lastIndexOf(str);
        return index === -1 ? source : source.substring(index + str.length); // 修正：应为 index + str.length
    }

    /**
     * 截取字符串，返回子字符串前面的部分。如果不包含，则原样返回
     * @param s 原始字符串
     * @param sub 分隔符
     * @returns 分隔符前面的字符串，或原字符串
     */
    static subBefore(s: string | null | undefined, sub: string): string | null | undefined {
        if (s == null) {
            return s;
        }
        const index = s.indexOf(sub);
        return index === -1 ? s : s.substring(0, index);
    }

    /**
     * 混淆字符串 (通过在每个字符后添加 '1')
     * @param str 原始字符串
     * @returns 混淆后的字符串
     */
    static obfuscateString(str: string | null | undefined): string | null | undefined {
        if (str == null) {
            return str;
        }
        return str.split('').map(char => char + '1').join('');
    }

    /**
     * 补零或补指定字符
     * @param input 输入值（字符串或数字）
     * @param totalLen 总长度
     * @param padChar 填充字符，默认为 '0'
     * @returns 填充后的字符串
     */
    static pad(input: string | number | null | undefined, totalLen: number, padChar: string = '0'): string {
        if (input == null) {
            return padChar.repeat(totalLen);
        }
        let str = String(input);
        const charsNeeded: number = totalLen - str.length;
        if (charsNeeded > 0) {
            str = padChar.repeat(charsNeeded) + str;
        }
        return str;
    }

    /**
     * 简单加密：将每个字符的 ASCII 码加 1，并转换为四位十六进制表示
     * @param str 原始字符串
     * @returns 加密后的十六进制字符串
     */
    static encrypt(str: string | null | undefined): string | null | undefined {
        if (str == null) {
            return str;
        }
        let encrypted: string = '';
        for (let i = 0; i < str.length; i++) {
            let charCode: number = str.charCodeAt(i);
            charCode += 1;
            // 转换为四位的十六进制表示，并确保至少四位
            encrypted += ('0000' + charCode.toString(16)).slice(-4);
        }
        return encrypted;
    }

    /**
     * 简单解密：还原加密字符串
     * @param hexString 加密后的十六进制字符串
     * @returns 解密后的字符串
     */
    static decrypt(hexString: string | null | undefined): string | null | undefined {
        if (hexString == null) {
            return hexString;
        }
        let decrypted: string = '';
        // 确保是 4 的倍数，否则可能无法正确解密
        if (hexString.length % 4 !== 0) {
            // 应该抛出错误或根据业务逻辑处理，这里为兼容原 js 逻辑，简单返回
            return hexString;
        }

        for (let i = 0; i < hexString.length; i += 4) {
            const hexCharCode: string = hexString.substring(i, i + 4);
            let charCode: number = parseInt(hexCharCode, 16); // 将十六进制转换为十进制
            if (isNaN(charCode)) {
                // 如果解析失败，也应根据业务逻辑处理
                return hexString;
            }
            charCode -= 1;
            decrypted += String.fromCharCode(charCode);
        }
        return decrypted;
    }

    /**
     * 获取字符串的显示宽度：英文字符 1 宽，中文字符 2 宽
     * @param str 原始字符串
     * @returns 字符串的显示宽度
     */
    static getWidth(str: string | null | undefined): number {
        if (str == null || str.length === 0) {
            return 0;
        }
        // 使用非空断言 ! 简化
        return str!.split('').reduce((pre, cur) => {
            const charCode: number = cur.charCodeAt(0);
            // 假设 0-128 为半角字符
            if (charCode >= 0 && charCode <= 128) {
                return pre + 1;
            }
            // 其它视为全角字符
            return pre + 2;
        }, 0);
    }

    /**
     * 按显示宽度截取字符串
     * @param str 原始字符串
     * @param maxWidth 最大显示宽度
     * @returns 截取后的字符串
     */
    static cutByWidth(str: string, maxWidth: number): string {
        let showLength: number = 0;
        return str.split('').reduce((pre, cur) => {
            const charCode: number = cur.charCodeAt(0);
            let charWidth: number = (charCode >= 0 && charCode <= 128) ? 1 : 2;

            if (showLength + charWidth <= maxWidth) {
                showLength += charWidth;
                return pre + cur;
            }
            return pre;
        }, '');
    }


    /**
     * 字符串省略处理（按显示宽度计算）
     * @param str 原始字符串
     * @param len 字符长度（宽度），注：中文字符算 2
     * @param suffix 省略号后缀，默认为 '...'
     * @returns 处理后的字符串
     */
    static ellipsis(str: string | null | undefined, len: number, suffix: string = '...'): string | null | undefined {
        if (str == null) {
            return str;
        }
        if (!StringUtils.isStr(str)) { // 使用类方法调用
            return str;
        }

        // 快速判断
        if (str.length * 2 < len) {
            return str;
        }

        const fullLength: number = StringUtils.getWidth(str);

        if (fullLength <= len) {
            return str;
        }

        // 截取
        return StringUtils.cutByWidth(str, len) + suffix;
    }

    /**
     * 判断值是否为字符串类型
     * @param value 任意值
     * @returns 是否为字符串
     */
    static isStr(value: any): value is string {
        return typeof value === 'string';
    }


    /**
     * 将下划线或连字符分隔的字符串转为驼峰命名
     * @param str 原始字符串
     * @param firstLower 转换后首字母是否小写，默认为 true
     * @returns 转换后的驼峰字符串
     */
    static toCamelCase(str: string, firstLower: boolean = true): string {
        // 匹配下划线或连字符后跟一个字母
        let result: string = str.replace(/[-_](\w)/g, function (all, letter) {
            return letter.toUpperCase();
        });

        if (firstLower) {
            // 确保首字母小写
            result = result.substring(0, 1).toLowerCase() + result.substring(1);
        }

        return result;
    }

    /**
     * 将驼峰命名字符串转为下划线命名
     * @param name 驼峰命名字符串
     * @returns 下划线命名字符串
     */
    static toUnderlineCase(name: string | null | undefined): string | null | undefined {
        if (name == null) {
            return null;
        }
        // 在大写字母前添加下划线，并全部转小写
        let result: string = name.replace(/([A-Z])/g, '_$1').toLowerCase();

        // 移除开头多余的下划线
        if (result.startsWith('_')) {
            result = result.substring(1);
        }
        return result;
    }

    /**
     * 比较两个字符串是否相等，忽略大小写
     * @param a 字符串 a
     * @param b 字符串 b
     * @returns 是否相等（忽略大小写）
     */
    static equalsIgnoreCase(a: string | null | undefined, b: string | null | undefined): boolean {
        if (a === b) { // 引用相同，或同时为 null/undefined
            return true;
        }

        if (a != null && b != null) {
            return a.toLowerCase() === b.toLowerCase();
        }

        // 只有在 a === b 时，null/undefined 才是 true
        return false;
    }

    /**
     * 分割字符串
     * @param str
     * @param sp
     */
    static split(str: any, sp: string):null|string[] {
        if(str == null || str.length === 0){
            return undefined
        }
        if(Array.isArray(str)){
            return str;
        }

        return str.split(sp)
    }

    /**
     * 连接字符串
     * @param arr
     * @param sp 分隔符
     */
    static join(arr, sp: string) {
        if (arr == null) {
            return  []
        }

        if(!Array.isArray(arr)){
            return arr;
        }

        return arr.join(sp);
    }
}
