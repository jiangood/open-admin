package io.github.jiangood.openadmin.modules.mail.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.JakartaMail;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailException;
import io.github.jiangood.openadmin.framework.proxy.ProxySettings;
import io.github.jiangood.openadmin.modules.config.service.SysConfigService;
import io.github.jiangood.openadmin.modules.mail.MailAttachment;
import io.github.jiangood.openadmin.modules.mail.MailConfigKeys;
import io.github.jiangood.openadmin.modules.mail.MailMessage;
import io.github.jiangood.openadmin.modules.mail.MailProvider;
import io.github.jiangood.openadmin.modules.mail.MailProxyProperties;
import io.github.jiangood.openadmin.modules.mail.MailTemplate;
import io.github.jiangood.openadmin.util.BusinessException;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 框架邮件发送服务。
 *
 * <p>配置来自「系统管理 → 系统配置 → 邮件发送」，支持运行时修改，无需重启。
 * 业务模块只需注入本类并调用 {@link #send(MailMessage)} 或 {@link #sendAsync(MailMessage)}。</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final SysConfigService configService;

    /**
     * 同步发送，失败抛 {@link BusinessException}，适合交互式场景（测试发送等）。
     */
    public void send(MailMessage message) {
        if (message == null) {
            throw new BusinessException("邮件内容不能为空");
        }
        List<String> to = clean(message.getTo());
        if (to.isEmpty()) {
            throw new BusinessException("收件邮箱不能为空");
        }
        if (StrUtil.isBlank(message.getSubject())) {
            throw new BusinessException("邮件主题不能为空");
        }
        if (StrUtil.isBlank(message.getHtml())) {
            throw new BusinessException("邮件内容不能为空");
        }

        Settings settings = settings();
        MailAccount account = createAccount(settings);
        JakartaMail mail = JakartaMail.create(account)
                .setTos(to.toArray(String[]::new))
                .setTitle(message.getSubject())
                .setContent(message.getHtml(), true);

        List<String> cc = clean(message.getCc());
        if (!cc.isEmpty()) {
            mail.setCcs(cc.toArray(String[]::new));
        }
        List<String> bcc = clean(message.getBcc());
        if (!bcc.isEmpty()) {
            mail.setBccs(bcc.toArray(String[]::new));
        }

        List<DataSource> attachments = attachments(message.getAttachments());
        if (!attachments.isEmpty()) {
            mail.setAttachments(attachments.toArray(DataSource[]::new));
        }

        try {
            mail.send();
        } catch (MailException e) {
            throw new BusinessException(failureMessage(e, settings));
        }

        log.info("邮件发送成功，收件人：{}，主题：{}，附件：{}，代理：{}",
                to, message.getSubject(), attachments.size(),
                settings.proxy().enabled() ? settings.proxy().address() : "无");
    }

    /**
     * 异步发送：失败只记录日志，不影响调用方（适合事件通知）。
     */
    @Async
    public void sendAsync(MailMessage message) {
        try {
            send(message);
        } catch (Exception e) {
            log.warn("邮件发送失败：{}", e.getMessage());
        }
    }

    /** 发到发件邮箱自己，用于验证配置。 */
    public void sendTest() {
        Settings settings = settings();
        MailMessage message = MailMessage.builder()
                .to(settings.username())
                .subject("邮件通知测试")
                .html(MailTemplate.test())
                .build();
        send(message);
    }

    public boolean isConfigured() {
        try {
            settings();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Settings settings() {
        MailProvider provider = MailProvider.parse(configService.getString(MailConfigKeys.PROVIDER));
        if (provider == null) {
            throw new BusinessException("未配置邮箱类型，请到「系统管理 → 系统配置 → 邮件发送」选择");
        }

        String username = StrUtil.trimToNull(configService.getString(MailConfigKeys.USERNAME));
        if (username == null) {
            throw new BusinessException("未配置发件邮箱，请到「系统管理 → 系统配置 → 邮件发送」填写");
        }
        String password = StrUtil.trimToNull(configService.getString(MailConfigKeys.PASSWORD));
        if (password == null) {
            throw new BusinessException("未配置邮箱授权码，请到「系统管理 → 系统配置 → 邮件发送」填写");
        }

        ProxySettings proxy;
        try {
            proxy = ProxySettings.parse(configService.getString(MailConfigKeys.PROXY));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("邮件代理地址不正确：" + e.getMessage());
        }
        if (proxy.type() == ProxySettings.Type.SOCKS5 && StrUtil.isNotBlank(proxy.username())) {
            throw new BusinessException("SOCKS5 代理不支持账号密码，需要认证请改用 http://用户名:密码@主机:端口");
        }

        return new Settings(provider, username, password,
                StrUtil.trimToNull(configService.getString(MailConfigKeys.FROM_NAME)), proxy);
    }

    private MailAccount createAccount(Settings settings) {
        MailProvider provider = settings.provider();
        String from = StrUtil.isBlank(settings.fromName())
                ? settings.username()
                : settings.fromName() + " <" + settings.username() + ">";

        MailAccount account = new MailAccount()
                .setHost(provider.host())
                .setPort(provider.port())
                .setAuth(true)
                .setSslEnable(provider.ssl())
                .setStarttlsEnable(provider.starttls())
                .setFrom(from)
                .setUser(settings.username())
                .setPass(settings.password())
                .setCharset(StandardCharsets.UTF_8)
                .setConnectionTimeout(10_000)
                .setTimeout(20_000)
                .setWriteTimeout(20_000);

        MailProxyProperties.apply(account, settings.proxy());
        return account;
    }

    private List<DataSource> attachments(List<MailAttachment> attachments) {
        List<DataSource> result = new ArrayList<>();
        if (attachments == null) {
            return result;
        }
        for (MailAttachment attachment : attachments) {
            if (attachment == null) {
                continue;
            }
            String name = StrUtil.blankToDefault(attachment.name(), "attachment");
            if (attachment.file() != null) {
                result.add(new NamedFileDataSource(attachment.file(), name));
            } else if (attachment.bytes() != null) {
                ByteArrayDataSource source = new ByteArrayDataSource(attachment.bytes(),
                        StrUtil.blankToDefault(attachment.contentType(), "application/octet-stream"));
                source.setName(name);
                result.add(source);
            }
        }
        return result;
    }

    private static List<String> clean(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(StrUtil::trimToNull)
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    private static String failureMessage(MailException error, Settings settings) {
        Throwable cause = error.getCause() == null ? error : error.getCause();
        String message = StrUtil.blankToDefault(cause.getMessage(), cause.getClass().getSimpleName());
        ProxySettings proxy = settings.proxy();
        if (proxy.enabled() && mentionsProxy(cause)) {
            return "邮件代理 " + proxy.address() + " 连接失败：" + message;
        }
        if (settings.provider() == MailProvider.OUTLOOK
                && message.toLowerCase(Locale.ROOT).contains("auth")) {
            return "邮件发送失败：" + message
                    + "（Outlook / Microsoft 365 需开启 SMTP AUTH；个人 Outlook.com 已停用基础认证）";
        }
        return "邮件发送失败：" + message;
    }

    private static boolean mentionsProxy(Throwable error) {
        Throwable current = error;
        for (int depth = 0; current != null && depth < 10; depth++) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains("proxy")) {
                return true;
            }
            if (current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }
        return false;
    }

    private record Settings(MailProvider provider, String username, String password,
                            String fromName, ProxySettings proxy) {
    }

    /** 保留自定义附件名的文件数据源，避免只能使用文件本名。 */
    private static final class NamedFileDataSource extends FileDataSource {

        private final String name;

        private NamedFileDataSource(java.io.File file, String name) {
            super(file);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
