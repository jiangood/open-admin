package io.github.jiangood.openadmin.framework.spi;

import java.time.Instant;
import java.util.Map;

/**
 * 直传预签名信息（单文件 PUT 或分片上传的 part PUT）
 */
public class DirectUploadInfo {

    private String url;
    private String method = "PUT";
    private Map<String, String> headers;
    private Instant expiresAt;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
