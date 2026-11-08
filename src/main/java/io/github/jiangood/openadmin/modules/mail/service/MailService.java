package io.github.jiangood.openadmin.modules.mail.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.JakartaMail;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailException;
import io.github.jiangood.openadmin.framework.proxy.ProxySettings;
import io.github.jiangood.openadmin.modules.config.service.SysConfigService;
import io.github.jiangood.openadmin.modules.mail.MailAttachment;
import io.github.jiangood.openadmin.modules.mail.MailConfigKeys;
import io.github.jiangood.openadmin.modules.mail.MailErrorType;
import io.github.jiangood.openadmin.modules.mail.MailInline;
import io.github.jiangood.openadmin.modules.mail.MailMessage;
import io.github.jiangood.openadmin.modules.mail.MailProvider;
import io.github.jiangood.openadmin.modules.mail.MailProxyProperties;
import io.github.jiangood.openadmin.modules.mail.MailSendResult;
import io.github.jiangood.openadmin.util.BusinessException;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.annotation.Resource;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 框架邮件发送服务。
 *
 * <p>配置来自「系统管理 → 参数设置 → 邮件发送」，支持运行时修改，无需重启。
 * 业务模块注入本类并调用 {@link #send(MailMessage)} / {@link #sendAsync(MailMessage)}；
 * 需要感知发送结果时使用 {@link #trySend(MailMessage)} / {@link #sendAsyncResult(MailMessage)}。</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final SysConfigService configService;

    @Resource(name = "mailExecutor")
    private Executor mailExecutor;

    /**
     * 同步发送，失败抛 {@link BusinessException}，适合需要立即感知发送结果的交互式场景。
     */
    public void send(MailMessage message) {
        try {
            sendInternal(message);
        } catch (SendFailure failure) {
            throw new BusinessException(failure.getMessage());
        }
    }

    /**
     * 同步发送，失败不抛异常，返回可观测的结果。
     *
     * <p>适合需要记录失败、决定是否重试或提示用户补配置的业务场景。
     * 校验、配置、认证、网络等失败都会被转换为 {@link MailErrorType}。</p>
     */
    public MailSendResult trySend(MailMessage message) {
        long start = System.nanoTime();
        try {
            sendInternal(message);
            return MailSendResult.ok(elapsedMillis(start));
        } catch (SendFailure failure) {
            return MailSendResult.fail(failure.type, failure.getMessage(), elapsedMillis(start));
        } catch (Exception e) {
            log.warn("邮件发送异常：{}", e.getMessage(), e);
            return MailSendResult.fail(MailErrorType.UNKNOWN, "邮件发送失败：" + safeMessage(e), elapsedMillis(start));
        }
    }

    /**
     * 异步发送：失败只记录日志，不影响调用方（适合事件通知）。
     */
    @Async("mailExecutor")
    public void sendAsync(MailMessage message) {
        MailSendResult result = trySend(message);
        if (!result.success()) {
            log.warn("邮件发送失败：{}", result.error());
        }
    }

    /**
     * 异步发送并返回结果，业务可通过 future 记录失败或重试。
     */
    public CompletableFuture<MailSendResult> sendAsyncResult(MailMessage message) {
        return CompletableFuture.supplyAsync(() -> trySend(message), mailExecutor);
    }

    private void sendInternal(MailMessage message) {
        if (message == null) {
            throw new SendFailure(MailErrorType.INVALID_MESSAGE, "邮件内容不能为空");
        }
        List<String> to = resolveRecipients(message);
        List<String> cc = clean(message.getCc());
        List<String> bcc = clean(message.getBcc());
        if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty()) {
            throw new SendFailure(MailErrorType.RECIPIENT_MISSING, "收件邮箱不能为空");
        }
        if (StrUtil.isBlank(message.getSubject())) {
            throw new SendFailure(MailErrorType.INVALID_MESSAGE, "邮件主题不能为空");
        }
        if (StrUtil.isBlank(message.getHtml())) {
            throw new SendFailure(MailErrorType.INVALID_MESSAGE, "邮件内容不能为空");
        }
        validateInlines(message.getInlines());

        Settings settings = settingsForSend();
        MailAccount account = createAccount(settings);
        if (to.isEmpty()) {
            // 只有抄送/密送时显式把发件人放进 To：部分服务器会把「没有 To」的邮件判定为无收件人而拒收
            to = List.of(settings.username());
        }

        JakartaMail mail = buildMail(account, message, to, cc, bcc);

        try {
            mail.send();
        } catch (MailException e) {
            throw failure(e, settings);
        }

        log.info("邮件发送成功，收件人：{}，主题：{}，附件：{}，内联图片：{}，代理：{}",
                to, message.getSubject(), attachments(message.getAttachments()).size(),
                message.getInlines() == null ? 0 : message.getInlines().size(),
                settings.proxy().enabled() ? settings.proxy().address() : "无");
    }

    /**
     * 组装邮件（不含实际发送），便于单测直接检查 MIME 结构。
     */
    static JakartaMail buildMail(MailAccount account, MailMessage message,
                                 List<String> to, List<String> cc, List<String> bcc) {
        JakartaMail mail = JakartaMail.create(account)
                .setTos(to.toArray(String[]::new))
                .setTitle(message.getSubject())
                .setContent(message.getHtml(), true);

        if (!cc.isEmpty()) {
            mail.setCcs(cc.toArray(String[]::new));
        }
        if (!bcc.isEmpty()) {
            mail.setBccs(bcc.toArray(String[]::new));
        }

        List<DataSource> attachments = attachments(message.getAttachments());
        if (!attachments.isEmpty()) {
            mail.setAttachments(attachments.toArray(DataSource[]::new));
        }

        addInlines(mail, message.getInlines());
        return mail;
    }

    private Settings settingsForSend() {
        try {
            return settings();
        } catch (BusinessException e) {
            throw new SendFailure(MailErrorType.NOT_CONFIGURED, e.getMessage());
        }
    }

    private List<String> resolveRecipients(MailMessage message) {
        List<String> to = clean(message.getTo());
        if (!to.isEmpty()) {
            return to;
        }
        return clean(configService.getList(MailConfigKeys.DEFAULT_TO));
    }

    private static long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private static String safeMessage(Throwable error) {
        String message = error == null ? null : error.getMessage();
        return StrUtil.blankToDefault(message, error == null ? "未知错误" : error.getClass().getSimpleName());
    }

    /** 内部失败信号，对 {@link #send} 转换为 {@link BusinessException}，对 {@link #trySend} 转换为结果。 */
    private static final class SendFailure extends RuntimeException {

        private final MailErrorType type;

        private SendFailure(MailErrorType type, String message) {
            super(message);
            this.type = type;
        }
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
            throw new BusinessException("未配置邮箱类型，请到「系统管理 → 参数设置 → 邮件发送」选择");
        }

        String username = StrUtil.trimToNull(configService.getString(MailConfigKeys.USERNAME));
        if (username == null) {
            throw new BusinessException("未配置发件邮箱，请到「系统管理 → 参数设置 → 邮件发送」填写");
        }
        String password = StrUtil.trimToNull(configService.getString(MailConfigKeys.PASSWORD));
        if (password == null) {
            throw new BusinessException("未配置邮箱授权码，请到「系统管理 → 参数设置 → 邮件发送」填写");
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

    private static List<DataSource> attachments(List<MailAttachment> attachments) {
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

    private static void validateInlines(List<MailInline> inlines) {
        if (inlines == null || inlines.isEmpty()) {
            return;
        }
        for (MailInline inline : inlines) {
            if (inline == null) {
                throw new SendFailure(MailErrorType.INVALID_MESSAGE, "内联图片内容不能为空");
            }
            if (StrUtil.isBlank(inline.cid())) {
                throw new SendFailure(MailErrorType.INVALID_MESSAGE, "内联图片 cid 不能为空");
            }
            if (inline.file() == null && inline.bytes() == null) {
                throw new SendFailure(MailErrorType.INVALID_MESSAGE, "内联图片文件和内容不能同时为空：" + inline.cid());
            }
        }
    }

    private static void addInlines(JakartaMail mail, List<MailInline> inlines) {
        if (inlines == null || inlines.isEmpty()) {
            return;
        }
        for (MailInline inline : inlines) {
            if (inline == null) {
                continue;
            }
            String cid = inline.cid();
            try (InputStream input = openInline(inline)) {
                // Content-ID 按 RFC 用尖括号包裹，正文仍用 cid:xxx 引用
                mail.addImage("<" + cid + ">", input, inlineContentType(inline));
            } catch (IOException e) {
                throw new SendFailure(MailErrorType.INVALID_MESSAGE, "内联图片读取失败：" + cid);
            }
        }
    }

    private static InputStream openInline(MailInline inline) throws IOException {
        if (inline.file() != null) {
            return new BufferedInputStream(new FileInputStream(inline.file()));
        }
        return new ByteArrayInputStream(inline.bytes());
    }

    private static String inlineContentType(MailInline inline) {
        String contentType = StrUtil.trimToNull(inline.contentType());
        if (contentType != null) {
            return contentType;
        }
        if (inline.file() != null) {
            String byName = URLConnection.guessContentTypeFromName(inline.file().getName());
            if (byName != null && byName.startsWith("image/")) {
                return byName;
            }
        }
        return "image/png";
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

    private static SendFailure failure(MailException error, Settings settings) {
        Throwable cause = error.getCause() == null ? error : error.getCause();
        String message = StrUtil.blankToDefault(cause.getMessage(), cause.getClass().getSimpleName());
        MailErrorType type = classifyFailure(cause, settings.proxy().enabled());
        if (type == MailErrorType.PROXY) {
            return new SendFailure(type, "邮件代理 " + settings.proxy().address() + " 连接失败：" + message);
        }
        if (type == MailErrorType.AUTH && settings.provider() == MailProvider.OUTLOOK) {
            return new SendFailure(type, "邮件发送失败：" + message
                    + "（Outlook / Microsoft 365 需开启 SMTP AUTH；个人 Outlook.com 已停用基础认证）");
        }
        return new SendFailure(type, "邮件发送失败：" + message);
    }

    /**
     * 根据异常链判断失败类型；代理优先于认证/网络，便于页面直接提示「代理连接失败」。
     */
    static MailErrorType classifyFailure(Throwable error, boolean proxyEnabled) {
        String messages = collectMessages(error);
        if (proxyEnabled && messages.contains("proxy")) {
            return MailErrorType.PROXY;
        }
        if (messages.contains("auth") || messages.contains("535") || messages.contains("login")
                || messages.contains("用户名") || messages.contains("密码")) {
            return MailErrorType.AUTH;
        }
        if (messages.contains("connect") || messages.contains("timeout") || messages.contains("timed out")
                || messages.contains("unknownhost") || messages.contains("unreachable")
                || messages.contains("network")) {
            return MailErrorType.NETWORK;
        }
        return MailErrorType.UNKNOWN;
    }

    private static String collectMessages(Throwable error) {
        StringBuilder sb = new StringBuilder();
        Throwable current = error;
        for (int depth = 0; current != null && depth < 10; depth++) {
            if (current.getMessage() != null) {
                sb.append(current.getMessage().toLowerCase(Locale.ROOT)).append(' ');
            }
            if (current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }
        return sb.toString();
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
