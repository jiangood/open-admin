package io.github.jiangood.openadmin.modules.mail;

import io.github.jiangood.openadmin.util.dto.Option;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailProviderTest {

    @Test
    void optionsCoverEveryProvider() {
        List<Option> options = MailProvider.options();

        assertEquals(MailProvider.values().length, options.size());
        assertTrue(options.stream().anyMatch(option -> "qq".equals(option.getValue())));
        assertTrue(options.stream().anyMatch(option -> "outlook".equals(option.getValue())));
        // 选项名称里带服务器信息，配置页不需要单独展示 host/port
        assertTrue(options.stream()
                .anyMatch(option -> String.valueOf(option.getLabel()).contains("smtp.qq.com:465/SSL")));
    }

    @Test
    void parsesCodeIgnoringCaseAndSpace() {
        assertEquals(MailProvider.QQ, MailProvider.parse(" QQ "));
        assertEquals(MailProvider.OUTLOOK, MailProvider.parse("outlook"));
        assertNull(MailProvider.parse("custom"));
        assertNull(MailProvider.parse(null));
    }

    @Test
    void resolvesByHost() {
        assertEquals(MailProvider.QQ, MailProvider.fromHost("smtp.qq.com"));
        assertEquals(MailProvider.GMAIL, MailProvider.fromHost("SMTP.GMAIL.COM"));
        assertNull(MailProvider.fromHost("mail.example.com"));
    }

    @Test
    void sslAndStarttlsAreExclusive() {
        for (MailProvider provider : MailProvider.values()) {
            assertNotNull(provider.host());
            assertTrue(provider.port() > 0);
            assertFalse(provider.ssl() && provider.starttls(), provider.name());
        }
    }
}
