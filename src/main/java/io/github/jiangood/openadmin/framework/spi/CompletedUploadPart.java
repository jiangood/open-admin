package io.github.jiangood.openadmin.framework.spi;

/**
 * 已完成分片的标识（用于 CompleteMultipartUpload）
 */
public class CompletedUploadPart {

    private int partNumber;
    private String etag;

    public CompletedUploadPart() {
    }

    public CompletedUploadPart(int partNumber, String etag) {
        this.partNumber = partNumber;
        this.etag = etag;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
