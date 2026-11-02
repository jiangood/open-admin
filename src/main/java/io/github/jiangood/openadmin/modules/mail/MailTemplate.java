package io.github.jiangood.openadmin.modules.mail;

import cn.hutool.core.util.StrUtil;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知邮件的通用 HTML 版式：标题 + 副标题 + 键值表格 + 结语。
 *
 * <p>邮件客户端普遍不支持 CSS 变量，这里只能用内联样式；
 * 这是框架内少数允许出现十六进制颜色的地方。</p>
 */
public final class MailTemplate {

    private static final String FONT = "-apple-system,BlinkMacSystemFont,'Segoe UI',Arial,sans-serif";

    public enum Status {
        SUCCESS("#16a34a"),
        ERROR("#dc2626"),
        INFO("#2563eb");

        private final String color;

        Status(String color) {
            this.color = color;
        }

        public String color() {
            return color;
        }
    }

    private MailTemplate() {
    }

    public static Builder notification(String title, Status status) {
        return new Builder(title, status == null ? Status.INFO : status);
    }

    public static Builder notification(String title) {
        return notification(title, Status.INFO);
    }

    /** 测试邮件内容，用于验证 SMTP 配置。 */
    public static String test() {
        return "<div style=\"font-family:" + FONT + ";color:#1f2937;font-size:14px;line-height:1.7\">"
                + "<h2 style=\"font-size:18px;margin:0 0 12px;color:" + Status.SUCCESS.color() + "\">邮件发送服务可用</h2>"
                + "<p>如果你收到这封邮件，说明邮件发送配置正确。业务模块可以通过 MailService 发送通知。</p>"
                + "</div>";
    }

    public static final class Builder {

        private final String title;
        private final Status status;
        private final List<String[]> rows = new ArrayList<>();
        private final List<String> paragraphs = new ArrayList<>();
        private String subtitle;

        private Builder(String title, Status status) {
            this.title = title;
            this.status = status;
        }

        public Builder subtitle(String value) {
            this.subtitle = value;
            return this;
        }

        public Builder row(String label, String value) {
            rows.add(new String[]{label, value});
            return this;
        }

        public Builder paragraph(String text) {
            if (StrUtil.isNotBlank(text)) {
                paragraphs.add(text);
            }
            return this;
        }

        public String build() {
            StringBuilder html = new StringBuilder();
            html.append("<div style=\"font-family:").append(FONT)
                    .append(";color:#1f2937;font-size:14px;line-height:1.7\">");

            html.append("<h2 style=\"font-size:18px;margin:0 0 4px;color:")
                    .append(status.color()).append("\">")
                    .append(escape(title)).append("</h2>");

            if (StrUtil.isNotBlank(subtitle)) {
                html.append("<p style=\"margin:0 0 16px;color:#6b7280\">")
                        .append(escape(subtitle)).append("</p>");
            } else if (!rows.isEmpty()) {
                html.append("<div style=\"height:12px\"></div>");
            }

            if (!rows.isEmpty()) {
                html.append("<table cellpadding=\"8\" cellspacing=\"0\" ")
                        .append("style=\"border-collapse:collapse;width:100%;max-width:680px;border:1px solid #e5e7eb\">");
                for (String[] row : rows) {
                    html.append("<tr>")
                            .append("<td style=\"width:110px;border:1px solid #e5e7eb;background:#f9fafb;")
                            .append("color:#4b5563;white-space:nowrap\">")
                            .append(escape(row[0])).append("</td>")
                            .append("<td style=\"border:1px solid #e5e7eb;word-break:break-all\">")
                            .append(escape(row[1])).append("</td>")
                            .append("</tr>");
                }
                html.append("</table>");
            }

            for (String paragraph : paragraphs) {
                html.append("<p style=\"margin:16px 0 0;color:#4b5563\">")
                        .append(escape(paragraph)).append("</p>");
            }

            html.append("</div>");
            return html.toString();
        }

        private static String escape(String value) {
            return StrUtil.isBlank(value) ? "-" : HtmlUtils.htmlEscape(value.trim());
        }
    }
}
