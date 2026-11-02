package io.github.jiangood.openadmin.modules.mail;

import cn.hutool.extra.mail.MailAccount;
import io.github.jiangood.openadmin.framework.proxy.ProxySettings;
import io.github.jiangood.openadmin.util.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailProxyPropertiesTest {

    @Test
    void disabledProxyHasNoProperties() {
        MailAccount account = new MailAccount();
        MailProxyProperties.apply(account, ProxySettings.parse(null));
        assertTrue(account.getCustomProperty().isEmpty());

        MailProxyProperties.apply(account, ProxySettings.parse("  "));
        assertTrue(account.getCustomProperty().isEmpty());
    }

    @Test
    void httpProxyMapsToAngusProperties() {
        Map<String, Object> props = props("http://user:pass@192.168.1.10:8080");

        assertEquals("192.168.1.10", props.get("mail.smtp.proxy.host"));
        assertEquals("8080", props.get("mail.smtp.proxy.port"));
        assertEquals("user", props.get("mail.smtp.proxy.user"));
        assertEquals("pass", props.get("mail.smtp.proxy.password"));
        assertFalse(props.containsKey("mail.smtp.socks.host"));
    }

    @Test
    void httpProxyWithoutCredentialsOnlySetsHostAndPort() {
        Map<String, Object> props = props("192.168.1.10:8080");

        assertEquals("192.168.1.10", props.get("mail.smtp.proxy.host"));
        assertFalse(props.containsKey("mail.smtp.proxy.user"));
        assertFalse(props.containsKey("mail.smtp.proxy.password"));
    }

    @Test
    void socks5ProxyHasNoAuthentication() {
        Map<String, Object> props = props("socks5://192.168.1.10:1080");

        assertEquals("192.168.1.10", props.get("mail.smtp.socks.host"));
        assertEquals("1080", props.get("mail.smtp.socks.port"));
        assertFalse(props.containsKey("mail.smtp.proxy.host"));
    }

    @Test
    void socks5WithCredentialsIsRejected() {
        ProxySettings proxy = ProxySettings.parse("socks5://user:pass@192.168.1.10:1080");
        assertThrows(BusinessException.class,
                () -> MailProxyProperties.apply(new MailAccount(), proxy));
    }

    private Map<String, Object> props(String url) {
        MailAccount account = new MailAccount();
        MailProxyProperties.apply(account, ProxySettings.parse(url));
        return account.getCustomProperty();
    }
}
