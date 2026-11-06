package io.github.jiangood.openadmin.modules.mail;

/**
 * 一次邮件发送的结果。
 *
 * @param success   是否发送成功
 * @param errorType 失败分类；成功时为 {@link MailErrorType#NONE}
 * @param error     失败原因；成功时为 {@code null}
 * @param elapsedMs 从开始校验到发送结束的耗时（毫秒）
 */
public record MailSendResult(boolean success, MailErrorType errorType, String error, long elapsedMs) {

    public static MailSendResult ok(long elapsedMs) {
        return new MailSendResult(true, MailErrorType.NONE, null, elapsedMs);
    }

    public static MailSendResult fail(MailErrorType errorType, String error, long elapsedMs) {
        return new MailSendResult(false, errorType, error, elapsedMs);
    }
}
