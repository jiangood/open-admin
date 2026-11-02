package io.github.jiangood.openadmin.modules.mail;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.util.dto.Option;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 内置邮箱服务商模板。
 *
 * <p>配置页只让用户选择服务商，SMTP 服务器、端口与加密方式由本枚举决定，
 * 避免让业务人员填写这些容易出错的参数。</p>
 */
public enum MailProvider {

    QQ("qq", "QQ 邮箱", "smtp.qq.com", 465, true, false),
    EXMAIL("exmail", "腾讯企业邮", "smtp.exmail.qq.com", 465, true, false),
    NET_163("163", "网易 163", "smtp.163.com", 465, true, false),
    NET_126("126", "网易 126", "smtp.126.com", 465, true, false),
    OUTLOOK("outlook", "Outlook / Microsoft 365", "smtp.office365.com", 587, false, true),
    GMAIL("gmail", "Gmail", "smtp.gmail.com", 587, false, true),
    ALIYUN("aliyun", "阿里企业邮", "smtp.qiye.aliyun.com", 465, true, false);

    private final String code;
    private final String label;
    private final String host;
    private final int port;
    private final boolean ssl;
    private final boolean starttls;

    MailProvider(String code, String label, String host, int port, boolean ssl, boolean starttls) {
        this.code = code;
        this.label = label;
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.starttls = starttls;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public boolean ssl() {
        return ssl;
    }

    public boolean starttls() {
        return starttls;
    }

    /** 下拉选项：名称里直接带上服务器信息，配置页不需要再展示 host/port 字段。 */
    public String optionLabel() {
        return label + "（" + host + ":" + port + "/" + (ssl ? "SSL" : "STARTTLS") + "）";
    }

    public static List<Option> options() {
        return Arrays.stream(values())
                .map(provider -> new Option(provider.code, provider.optionLabel()))
                .toList();
    }

    /** 按配置值解析；空值或未知值返回 null。 */
    public static MailProvider parse(String code) {
        String value = StrUtil.trimToEmpty(code).toLowerCase(Locale.ROOT);
        for (MailProvider provider : values()) {
            if (provider.code.equals(value)) {
                return provider;
            }
        }
        return null;
    }

    /** 按 SMTP 主机反推服务商（迁移旧配置用）；未知主机返回 null。 */
    public static MailProvider fromHost(String host) {
        String value = StrUtil.trimToEmpty(host).toLowerCase(Locale.ROOT);
        for (MailProvider provider : values()) {
            if (provider.host.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        return null;
    }
}
