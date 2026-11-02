package io.github.jiangood.openadmin.modules.mail;

import java.io.File;

/**
 * 邮件附件：文件或字节数组二选一。
 */
public record MailAttachment(String name, File file, byte[] bytes, String contentType) {

    public static MailAttachment of(File file) {
        return new MailAttachment(file.getName(), file, null, null);
    }

    public static MailAttachment of(String name, File file) {
        return new MailAttachment(name, file, null, null);
    }

    public static MailAttachment of(String name, byte[] bytes, String contentType) {
        return new MailAttachment(name, null, bytes, contentType);
    }
}
