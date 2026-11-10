package io.github.jiangood.openadmin.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 浏览器指纹工具。
 * <p>
 * 指纹由前端生成（本地缓存的设备特征哈希），随请求头 {@value #HEADER} 上报，
 * 用于在操作日志里判断多条记录是否来自同一浏览器。
 * <p>
 * 注意：请求头由客户端提供，可被伪造，仅作辅助排查，不能作为安全凭证。
 */
public class BrowserFingerprintTool {

    /**
     * 前端上报浏览器指纹的请求头名称
     */
    public static final String HEADER = "X-Browser-Fingerprint";

    /**
     * 与 sys_log.fingerprint 的列长度保持一致
     */
    public static final int MAX_LENGTH = 64;

    private BrowserFingerprintTool() {
    }

    /**
     * 从请求头读取浏览器指纹，去除首尾空白并按列长度截断。
     *
     * @return 指纹；请求为空、未携带或值为空白时返回 null
     */
    public static String get(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return normalize(request.getHeader(HEADER));
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > MAX_LENGTH ? trimmed.substring(0, MAX_LENGTH) : trimmed;
    }
}
