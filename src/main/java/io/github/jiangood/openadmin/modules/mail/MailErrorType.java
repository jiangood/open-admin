package io.github.jiangood.openadmin.modules.mail;

/**
 * 邮件发送失败分类。
 *
 * <p>业务侧可据此决定是否重试、是否提示用户去「系统参数」补配置，
 * 不需要解析错误文案。</p>
 */
public enum MailErrorType {

    /** 发送成功 */
    NONE,

    /** 邮件内容不合法：消息、主题或正文为空 */
    INVALID_MESSAGE,

    /** 收件人为空，且没有配置默认收件人 */
    RECIPIENT_MISSING,

    /** 邮件服务未配置或配置不完整 */
    NOT_CONFIGURED,

    /** 认证失败：账号/授权码错误，或服务商未开启 SMTP AUTH */
    AUTH,

    /** 代理连接失败 */
    PROXY,

    /** 网络连接失败、超时或域名无法解析 */
    NETWORK,

    /** 其他未分类错误 */
    UNKNOWN
}
