package io.github.jiangood.openadmin.modules.mail;

import java.io.File;

/**
 * 邮件内联图片：CID + 文件或字节数组二选一。
 *
 * <p>HTML 正文里用 {@code <img src="cid:cid">} 引用，客户端会直接显示图片而不是作为附件。
 * {@code cid} 可以写成 {@code logo}、{@code cid:logo} 或 {@code <logo>}，框架会统一成 {@code logo}。</p>
 */
public record MailInline(String cid, File file, byte[] bytes, String contentType) {

    public MailInline {
        cid = normalizeCid(cid);
    }

    public static MailInline of(String cid, File file) {
        return new MailInline(cid, file, null, null);
    }

    public static MailInline of(String cid, File file, String contentType) {
        return new MailInline(cid, file, null, contentType);
    }

    public static MailInline of(String cid, byte[] bytes, String contentType) {
        return new MailInline(cid, null, bytes, contentType);
    }

    static String normalizeCid(String cid) {
        if (cid == null) {
            return null;
        }
        String value = cid.trim();
        if (value.regionMatches(true, 0, "cid:", 0, 4)) {
            value = value.substring(4).trim();
        }
        if (value.length() >= 2 && value.startsWith("<") && value.endsWith(">")) {
            value = value.substring(1, value.length() - 1).trim();
        }
        return value.isEmpty() ? null : value;
    }
}
