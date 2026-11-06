package io.github.jiangood.openadmin.modules.mail.service;

import io.github.jiangood.openadmin.modules.config.service.SysConfigService;
import io.github.jiangood.openadmin.modules.mail.MailConfigKeys;
import io.github.jiangood.openadmin.modules.mail.MailErrorType;
import io.github.jiangood.openadmin.modules.mail.MailMessage;
import io.github.jiangood.openadmin.modules.mail.MailSendResult;
import io.github.jiangood.openadmin.util.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    void sendTest_usesDefaultRecipientsBeforeSenderAccount() {
        when(configService.getList(MailConfigKeys.DEFAULT_TO)).thenReturn(List.of("ops@example.com"));
        when(configService.getString(MailConfigKeys.PROVIDER)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, mailService::sendTest);

        // 已使用默认收件人，接下来才是 SMTP 未配置
        assertTrue(exception.getMessage().contains("未配置邮箱类型"));
    }

    @Test
    void sendTest_withExplicitRecipientDoesNotUseDefault() {
        when(configService.getString(MailConfigKeys.PROVIDER)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> mailService.sendTest("ops@example.com"));

        assertTrue(exception.getMessage().contains("未配置邮箱类型"));
        verify(configService, never()).getList(MailConfigKeys.DEFAULT_TO);
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

    private static MailMessage messageWithoutTo() {
        return MailMessage.builder()
                .subject("测试")
                .html("<p>content</p>")
                .build();
    }
}
