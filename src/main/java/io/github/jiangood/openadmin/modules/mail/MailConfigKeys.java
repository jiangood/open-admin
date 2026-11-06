package io.github.jiangood.openadmin.modules.mail;

/**
 * 邮件发送配置项 key。
 */
public final class MailConfigKeys {

    /** 邮箱服务商，决定 SMTP 服务器、端口与加密方式 */
    public static final String PROVIDER = "email.provider";

    /** 发件邮箱 */
    public static final String USERNAME = "email.username";

    /** 授权码/密码 */
    public static final String PASSWORD = "email.password";

    /** 发件人显示名称 */
    public static final String FROM_NAME = "email.from.name";

    /** 默认收件人，多个用逗号分隔；发送时未填收件人则使用 */
    public static final String DEFAULT_TO = "email.default-to";

    /** 代理地址，一个字段包含类型、主机、端口与凭据 */
    public static final String PROXY = "email.proxy";

    private MailConfigKeys() {
    }
}
