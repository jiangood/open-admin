package io.github.jiangood.openadmin.framework.proxy;

import cn.hutool.core.util.StrUtil;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 连接代理配置。
 *
 * <ul>
 *     <li>{@code HTTP}：向代理发 {@code CONNECT host:port} 建隧道，支持 Basic 认证；</li>
 *     <li>{@code SOCKS5}：RFC 1928 握手，可选 RFC 1929 用户名密码认证。</li>
 * </ul>
 *
 * <p>代理只转发 TCP 字节流：TLS 仍是平台与目标端到端，证书校验行为与直连一致。</p>
 *
 * <p>界面只填一行地址（{@link #parse(String)}）；回显统一用 {@link #displayUrl()}
 * （密码只给 {@link #PASSWORD_MASK}），保存时用 {@link #resolvePassword(String)} 还原。</p>
 */
public record ProxySettings(Type type, String host, Integer port, String username, String password) {

    /** 回显密码用的占位符：保存时原样出现表示沿用已保存的密码。 */
    public static final String PASSWORD_MASK = "***";

    public enum Type {
        NONE, HTTP, SOCKS5;

        /** 兼容前端与历史值：空 / 未知一律按不使用代理处理。 */
        public static Type parse(String value) {
            String v = StrUtil.trimToEmpty(value).toUpperCase(Locale.ROOT);
            return switch (v) {
                case "HTTP", "HTTPS" -> HTTP;
                case "SOCKS5", "SOCKS" -> SOCKS5;
                default -> NONE;
            };
        }

        public String code() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final ProxySettings DISABLED = new ProxySettings(Type.NONE, null, null, null, null);

    /**
     * 归一化：库里的原始值可能带空格、类型可能为空，这里统一处理，
     * 让 {@link #address()} / {@link #displayUrl()} 不必再各自 trim。
     */
    public ProxySettings {
        type = type == null ? Type.NONE : type;
        host = StrUtil.trimToNull(host);
        username = StrUtil.trimToNull(username);
        if (port != null && port <= 0) {
            port = null;
        }
    }

    public static ProxySettings disabled() {
        return DISABLED;
    }

    /** 由拆分字段组装（兼容历史数据与主机表）。 */
    public static ProxySettings of(String type, String host, Integer port, String username, String password) {
        return new ProxySettings(Type.parse(type), host, port, username, password);
    }

    /**
     * 解析单行代理地址：
     * <ul>
     *   <li>{@code http://192.168.1.10:8080}</li>
     *   <li>{@code http://user:pass@192.168.1.10:8080}</li>
     *   <li>{@code socks5://user:pass@192.168.1.10:1080}</li>
     * </ul>
     * 留空表示不使用代理；不写 scheme 时按 http 处理；端口必须显式给出。
     */
    public static ProxySettings parse(String url) {
        String raw = StrUtil.trimToEmpty(url);
        if (raw.isEmpty()) {
            return DISABLED;
        }

        String value = raw.contains("://") ? raw : "http://" + raw;
        int schemeEnd = value.indexOf("://");
        String scheme = value.substring(0, schemeEnd).trim().toLowerCase(Locale.ROOT);
        Type type = switch (scheme) {
            case "http", "https" -> Type.HTTP;
            case "socks", "socks5" -> Type.SOCKS5;
            default -> throw new IllegalArgumentException("代理地址只支持 http:// 或 socks5:// 前缀：" + raw);
        };

        String rest = value.substring(schemeEnd + 3).trim();
        // 误填的路径不影响解析
        int slash = rest.indexOf('/');
        if (slash >= 0) {
            rest = rest.substring(0, slash).trim();
        }
        Assert.isTrue(!rest.isEmpty(), "代理地址缺少主机，如 http://192.168.1.10:8080");

        // 以最后一个 @ 切分：密码里的 @ 不会把主机截断
        String userInfo = null;
        int at = rest.lastIndexOf('@');
        if (at >= 0) {
            userInfo = rest.substring(0, at);
            rest = rest.substring(at + 1).trim();
        }

        String host;
        String portText = null;
        if (rest.startsWith("[")) {
            // IPv6 字面量：[::1]:8080
            int close = rest.indexOf(']');
            Assert.isTrue(close > 1, "代理地址格式不正确：" + raw);
            host = rest.substring(1, close).trim();
            String after = rest.substring(close + 1).trim();
            if (!after.isEmpty()) {
                Assert.isTrue(after.startsWith(":"), "代理地址格式不正确：" + raw);
                portText = after.substring(1).trim();
            }
        } else {
            int colon = rest.lastIndexOf(':');
            if (colon >= 0) {
                portText = rest.substring(colon + 1).trim();
                host = rest.substring(0, colon).trim();
            } else {
                host = rest;
            }
        }
        Assert.hasText(host, "代理地址缺少主机，如 http://192.168.1.10:8080");
        Assert.hasText(portText, "代理地址需包含端口，如 http://192.168.1.10:8080");
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("代理端口不正确：" + portText);
        }
        Assert.isTrue(port > 0 && port <= 65535, "代理端口不正确：" + portText);

        String username = null;
        String password = null;
        if (StrUtil.isNotBlank(userInfo)) {
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                username = percentDecode(userInfo.substring(0, colon));
                password = percentDecode(userInfo.substring(colon + 1));
            } else {
                username = percentDecode(userInfo);
            }
            username = StrUtil.trimToNull(username);
            password = StrUtil.trimToNull(password);
        }

        return new ProxySettings(type, host, port, username, password);
    }

    public boolean enabled() {
        return type != null && type != Type.NONE;
    }

    /** 配置是否完整到可以真正建连（启用代理时地址与端口都必须有值）。 */
    public boolean configured() {
        return enabled() && StrUtil.isNotBlank(host) && port != null && port > 0;
    }

    /** 代理连接串，用于日志与错误提示（不含凭据）。 */
    public String address() {
        return StrUtil.trimToEmpty(host) + ":" + (port == null ? "" : port);
    }

    /** 回显用的单行地址；已保存的密码只给 {@link #PASSWORD_MASK}，不把明文送到前端。 */
    public String displayUrl() {
        if (!enabled()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(type == Type.SOCKS5 ? "socks5" : "http").append("://");
        if (StrUtil.isNotBlank(username)) {
            sb.append(percentEncode(username));
            if (password != null) {
                sb.append(':').append(PASSWORD_MASK);
            }
            sb.append('@');
        }
        appendHostPort(sb);
        return sb.toString();
    }

    /**
     * 落库用的完整地址（含明文密码）。密码只存在于数据库与内存，
     * 接口回显始终使用 {@link #displayUrl()}。
     */
    public String toUrl() {
        if (!enabled()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(type == Type.SOCKS5 ? "socks5" : "http").append("://");
        if (StrUtil.isNotBlank(username)) {
            sb.append(percentEncode(username));
            if (password != null) {
                sb.append(':').append(percentEncode(password));
            }
            sb.append('@');
        }
        appendHostPort(sb);
        return sb.toString();
    }

    /**
     * 保存时的密码策略：
     * <ul>
     *   <li>没有用户名就是匿名代理，密码一律清空（库里的残留也不能留）；</li>
     *   <li>密码写成 {@link #PASSWORD_MASK} 或省略，表示沿用 {@code storedPassword}；</li>
     *   <li>其它情况就是用户新填的密码。</li>
     * </ul>
     */
    public String resolvePassword(String storedPassword) {
        if (StrUtil.isBlank(username)) {
            return null;
        }
        if (password == null || PASSWORD_MASK.equals(password)) {
            return StrUtil.trimToNull(storedPassword);
        }
        return password;
    }

    private void appendHostPort(StringBuilder sb) {
        String h = StrUtil.trimToEmpty(host);
        if (h.contains(":") && !h.startsWith("[")) {
            sb.append('[').append(h).append(']');
        } else {
            sb.append(h);
        }
        if (port != null) {
            sb.append(':').append(port);
        }
    }

    /** 只处理 %XX，不把 '+' 当空格：代理密码里 '+' 很常见。 */
    private static String percentDecode(String value) {
        if (value == null || value.indexOf('%') < 0) {
            return value;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '%' && i + 2 < value.length()) {
                try {
                    out.write(Integer.parseInt(value.substring(i + 1, i + 3), 16));
                    i += 2;
                    continue;
                } catch (NumberFormatException ignored) {
                    // 不是合法的 %XX，按普通字符处理
                }
            }
            byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
            out.write(bytes, 0, bytes.length);
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    /** 只保留 RFC 3986 unreserved 字符，其余按 UTF-8 百分号编码。 */
    private static String percentEncode(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xFF;
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '.' || c == '_' || c == '~') {
                sb.append((char) c);
            } else {
                sb.append('%').append(HEX[c >> 4]).append(HEX[c & 0x0F]);
            }
        }
        return sb.toString();
    }

    /**
     * 不打印密码：record 默认 toString 会把所有字段拼出来，日志里一旦出现就等于泄露。
     */
    @Override
    public String toString() {
        return "ProxySettings[type=" + type + ", host=" + host + ", port=" + port
                + ", username=" + username + "]";
    }
}
