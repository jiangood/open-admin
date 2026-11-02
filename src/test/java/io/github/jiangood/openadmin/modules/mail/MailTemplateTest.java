package io.github.jiangood.openadmin.modules.mail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailTemplateTest {

    @Test
    void notificationEscapesValues() {
        String html = MailTemplate.notification("构建<script>", MailTemplate.Status.ERROR)
                .subtitle("demo")
                .row("项目", "a<b")
                .paragraph("已完成")
                .build();

        assertTrue(html.contains("构建&lt;script&gt;"));
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("a&lt;b"));
        assertTrue(html.contains(MailTemplate.Status.ERROR.color()));
        assertTrue(html.contains("已完成"));
    }

    @Test
    void blankRowValueBecomesDash() {
        String html = MailTemplate.notification("通知")
                .row("项目", "  ")
                .build();

        assertTrue(html.contains("<td style=\"border:1px solid #e5e7eb;word-break:break-all\">-</td>"));
    }

    @Test
    void testMailHasContent() {
        String html = MailTemplate.test();

        assertTrue(html.contains("邮件发送服务可用"));
        assertTrue(html.contains("MailService"));
    }
}
