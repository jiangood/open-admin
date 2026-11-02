package io.github.jiangood.openadmin.modules.mail;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import io.github.jiangood.openadmin.framework.proxy.ProxySettings;
import io.github.jiangood.openadmin.util.BusinessException;

/**
 * 把 {@link ProxySettings} 翻译成 Angus Mail 认识的代理属性。
 *
 * <ul>
 *   <li>HTTP：{@code mail.smtp.proxy.*}，由 JavaMail 发 {@code CONNECT} 建隧道，支持 Basic 认证；</li>
 *   <li>SOCKS5：{@code mail.smtp.socks.*}，走 JDK 的 SOCKS 实现，<b>不支持账号密码</b>。</li>
 * </ul>
 */
public final class MailProxyProperties {

    private MailProxyProperties() {
    }

    public static void apply(MailAccount account, ProxySettings proxy) {
        if (proxy == null || !proxy.configured()) {
            return;
        }

        if (proxy.type() == ProxySettings.Type.SOCKS5) {
            if (StrUtil.isNotBlank(proxy.username())) {
                throw new BusinessException("SOCKS5 代理不支持账号密码，需要认证请改用 http://用户名:密码@主机:端口");
            }
            account.setCustomProperty("mail.smtp.socks.host", proxy.host())
                    .setCustomProperty("mail.smtp.socks.port", String.valueOf(proxy.port()));
            return;
        }

        account.setCustomProperty("mail.smtp.proxy.host", proxy.host())
                .setCustomProperty("mail.smtp.proxy.port", String.valueOf(proxy.port()));
        if (StrUtil.isNotBlank(proxy.username())) {
            // JavaMail 只在 user 与 password 都非 null 时才发 Proxy-Authorization
            account.setCustomProperty("mail.smtp.proxy.user", proxy.username())
                    .setCustomProperty("mail.smtp.proxy.password", StrUtil.nullToEmpty(proxy.password()));
        }
    }
}
