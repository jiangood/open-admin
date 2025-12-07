package io.admin.modules.system.service;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private SysConfigService sysConfigService;

    @InjectMocks
    private EmailService emailService;

    @Test
    public void testSendEmail() {
        // 准备测试数据
        String to = "test@example.com";
        String title = "Test Email";
        String content = "This is a test email.";
        File file = new File("test.txt");

        // 设置mock行为
        when(sysConfigService.get("email.from")).thenReturn("sender@example.com");
        when(sysConfigService.get("email.pass")).thenReturn("password");

        // 执行测试
        emailService.send(to, title, content, file);

        // 验证行为
        verify(sysConfigService, times(2)).get(anyString());
        verify(sysConfigService).get("email.from");
        verify(sysConfigService).get("email.pass");
        
        // 注意：实际的MailUtil.send调用可能会导致异常，因为没有配置真实的邮件服务器
        // 在测试环境中，我们主要验证配置值是否被正确获取
    }

    @Test
    public void testSendEmailWithNullFiles() {
        // 准备测试数据
        String to = "test@example.com";
        String title = "Test Email";
        String content = "This is a test email.";

        // 设置mock行为
        when(sysConfigService.get("email.from")).thenReturn("sender@example.com");
        when(sysConfigService.get("email.pass")).thenReturn("password");

        // 执行测试
        emailService.send(to, title, content); // 不传递文件参数

        // 验证行为
        verify(sysConfigService, times(2)).get(anyString());
        verify(sysConfigService).get("email.from");
        verify(sysConfigService).get("email.pass");
    }
}