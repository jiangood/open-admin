package io.github.jiangood.openadmin.framework.spi;

/**
 * 对象存储中的文件元数据（用于直传完成后的校验）
 */
public class ObjectMetadata {

    private boolean exists;
    private long contentLength;
    private String contentType;
    private String etag;

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
