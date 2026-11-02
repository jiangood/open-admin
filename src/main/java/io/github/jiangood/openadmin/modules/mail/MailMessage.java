package io.github.jiangood.openadmin.modules.mail;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

/**
 * 一封待发送的邮件。
 *
 * <pre>{@code
 * MailMessage.builder()
 *     .to("ops@example.com")
 *     .subject("通知")
 *     .html("<p>内容</p>")
 *     .build();
 * }</pre>
 */
@Getter
@Builder
public class MailMessage {

    @Singular("to")
    private List<String> to;

    @Singular("cc")
    private List<String> cc;

    @Singular("bcc")
    private List<String> bcc;

    private String subject;

    /** HTML 正文 */
    private String html;

    @Singular("attachment")
    private List<MailAttachment> attachments;
}
