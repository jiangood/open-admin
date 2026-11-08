package io.github.jiangood.openadmin.modules.mail.service;

import cn.hutool.extra.mail.JakartaMail;
import cn.hutool.extra.mail.MailAccount;
import io.github.jiangood.openadmin.modules.config.service.SysConfigService;
import io.github.jiangood.openadmin.modules.mail.MailConfigKeys;
import io.github.jiangood.openadmin.modules.mail.MailErrorType;
import io.github.jiangood.openadmin.modules.mail.MailInline;
import io.github.jiangood.openadmin.modules.mail.MailMessage;
import io.github.jiangood.openadmin.modules.mail.MailSendResult;
import io.github.jiangood.openadmin.util.BusinessException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private SysConfigService configService;

    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailService(configService);
        ReflectionTestUtils.setField(mailService, "mailExecutor", (Executor) Runnable::run);
    }

    @Test
    void trySend_nullMessage_returnsInvalidMessage() {
        MailSendResult result = mailService.trySend(null);

        assertFalse(result.success());
        assertEquals(MailErrorType.INVALID_MESSAGE, result.errorType());
        assertEquals("邮件内容不能为空", result.error());
    }

    @Test
    void trySend_withoutRecipientsAndDefault_returnsRecipientMissing() {
        when(configService.getList(MailConfigKeys.DEFAULT_TO)).thenReturn(List.of());

        MailSendResult result = mailService.trySend(messageWithoutTo());

        assertFalse(result.success());
        assertEquals(MailErrorType.RECIPIENT_MISSING, result.errorType());
        assertEquals("收件邮箱不能为空", result.error());
    }

    @Test
    void trySend_defaultRecipientsPassValidation_thenReportsNotConfigured() {
        when(configService.getList(MailConfigKeys.DEFAULT_TO)).thenReturn(List.of("ops@example.com"));
        when(configService.getString(MailConfigKeys.PROVIDER)).thenReturn(null);

        MailSendResult result = mailService.trySend(messageWithoutTo());

        assertFalse(result.success());
        // 如果默认收件人没有生效，这里会是 RECIPIENT_MISSING
        assertEquals(MailErrorType.NOT_CONFIGURED, result.errorType());
        assertTrue(result.error().contains("未配置邮箱类型"));
    }

    @Test
    void trySend_blankSubject_returnsInvalidMessage() {
        MailMessage message = MailMessage.builder()
                .to("ops@example.com")
                .html("<p>content</p>")
                .build();

        MailSendResult result = mailService.trySend(message);

        assertFalse(result.success());
        assertEquals(MailErrorType.INVALID_MESSAGE, result.errorType());
        assertEquals("邮件主题不能为空", result.error());
    }

    @Test
    void send_withoutRecipientsAndDefault_throwsBusinessException() {
        when(configService.getList(MailConfigKeys.DEFAULT_TO)).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> mailService.send(messageWithoutTo()));

        assertEquals("收件邮箱不能为空", exception.getMessage());
    }

    @Test
    void sendAsyncResult_returnsObservableResult() throws Exception {
        when(configService.getList(MailConfigKeys.DEFAULT_TO)).thenReturn(List.of("ops@example.com"));
        when(configService.getString(MailConfigKeys.PROVIDER)).thenReturn(null);

        MailSendResult result = mailService.sendAsyncResult(messageWithoutTo())
                .get(2, TimeUnit.SECONDS);

        assertNotNull(result);
        assertFalse(result.success());
        assertEquals(MailErrorType.NOT_CONFIGURED, result.errorType());
    }

    @Test
    void classifyFailure_mapsCommonSmtpErrors() {
        assertEquals(MailErrorType.AUTH,
                MailService.classifyFailure(new RuntimeException("535 Authentication failed"), false));
        assertEquals(MailErrorType.NETWORK,
                MailService.classifyFailure(new RuntimeException("Connection timed out"), false));
        assertEquals(MailErrorType.PROXY,
                MailService.classifyFailure(new RuntimeException("proxy connect error"), true));
        assertEquals(MailErrorType.UNKNOWN,
                MailService.classifyFailure(new RuntimeException("unexpected error"), false));
    }

    @Test
    void trySend_bccOnly_thenReportsNotConfigured() {
        when(configService.getList(MailConfigKeys.DEFAULT_TO)).thenReturn(List.of());
        when(configService.getString(MailConfigKeys.PROVIDER)).thenReturn(null);

        MailMessage message = MailMessage.builder()
                .bcc("audit@example.com")
                .subject("测试")
                .html("<p>content</p>")
                .build();

        MailSendResult result = mailService.trySend(message);

        // 只有密送也算有收件人，不能再报 RECIPIENT_MISSING
        assertFalse(result.success());
        assertEquals(MailErrorType.NOT_CONFIGURED, result.errorType());
    }

    @Test
    void trySend_inlineWithoutCid_returnsInvalidMessage() {
        MailMessage message = MailMessage.builder()
                .to("ops@example.com")
                .subject("测试")
                .html("<p>content</p>")
                .inline(MailInline.of(" ", new byte[]{1}, "image/png"))
                .build();

        MailSendResult result = mailService.trySend(message);

        assertFalse(result.success());
        assertEquals(MailErrorType.INVALID_MESSAGE, result.errorType());
        assertEquals("内联图片 cid 不能为空", result.error());
    }

    @Test
    void buildMail_attachesInlineImageWithContentId() throws Exception {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        MailMessage message = MailMessage.builder()
                .subject("报告")
                .html("<p>见下图</p><img src=\"cid:chart\">")
                .inline(MailInline.of("cid:<chart>", png, "image/png"))
                .build();

        MailAccount account = new MailAccount()
                .setHost("localhost")
                .setFrom("sender@example.com")
                .setCharset(StandardCharsets.UTF_8);
        JakartaMail mail = MailService.buildMail(account, message,
                List.of("to@example.com"), List.of(), List.of());

        MimeMessage mime = mail.build();
        mime.saveChanges();
        MimeMultipart multipart = (MimeMultipart) mime.getContent();
        MimeBodyPart inlinePart = null;
        for (int i = 0; i < multipart.getCount(); i++) {
            MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
            if ("<chart>".equals(part.getContentID())) {
                inlinePart = part;
            }
        }

        assertNotNull(inlinePart);
        assertEquals("inline", inlinePart.getDisposition());
        assertTrue(inlinePart.getContentType().startsWith("image/png"), inlinePart.getContentType());
    }

    private static MailMessage messageWithoutTo() {
        return MailMessage.builder()
                .subject("测试")
                .html("<p>content</p>")
                .build();
    }
}
