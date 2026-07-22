/**
 * 验证工具类
 */
export class ValidateUtils {

    /**
     * 检查给定的字符串是否为有效的电子邮件地址。
     * @param emailStr 要检查的字符串。
     * @returns 如果是有效的电子邮件地址，则返回 true；否则返回 false。
     */
    public static isEmail(emailStr: string): boolean {
        // 匹配大多数常见电子邮件格式的正则表达式
        const reg = /^([\w+.-])+@\w+([.]\w+)+$/;
        return reg.test(emailStr);
    }

    // 可以在这里添加其他验证方法，例如：
    /*
    public static isPhone(phoneStr: string): boolean {
        // ... 手机号验证逻辑
        return /^\d{10}$/.test(phoneStr);
    }
    */
}

// 示例用法：
// const isValid = ValidateUtils.isEmail("test@example.com");
// console.log(isValid); // true
