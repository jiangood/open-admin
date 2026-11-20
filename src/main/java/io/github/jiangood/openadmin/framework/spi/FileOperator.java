package io.github.jiangood.openadmin.framework.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

public interface FileOperator {

    void save(String key, InputStream inputStream) throws IOException;

    void saveFile(String key, File file) throws IOException;

    InputStream getFileStream(String key) throws IOException;

    void downloadFile(String key, File target) throws IOException;

    void delete(String key) throws IOException;

    boolean exist(String key);

    default Object getClient() {
        throw new UnsupportedOperationException();
    }

    /**
     * 是否支持生成直链下载/预览地址（如 S3 预签名 GET），用于播放/下载时前端直连对象存储。
     */
    default boolean supportsPresignedGet() {
        return false;
    }

    /**
     * 生成直链 GET URL（如 S3 预签名 URL）。调用前应先判断 {@link #supportsPresignedGet()}。
     *
     * @param key 对象 key
     * @param ttl 有效期
     * @return 可直接访问的 URL
     */
    default String presignGetUrl(String key, Duration ttl) {
        throw new UnsupportedOperationException("不支持直链下载");
    }

    /**
     * 是否支持前端直传（预签名 PUT / 分片上传），用于绕过应用服务器带宽。
     */
    default boolean supportsDirectUpload() {
        return false;
    }

    /**
     * 生成单文件直传（PUT）预签名信息。
     */
    default DirectUploadInfo presignPut(String key, String contentType, Duration ttl) {
        throw new UnsupportedOperationException("不支持直传上传");
    }

    /**
     * 初始化分片上传，返回 uploadId。
     */
    default String initMultipartUpload(String key, String contentType) {
        throw new UnsupportedOperationException("不支持分片直传");
    }

    /**
     * 生成指定分片的直传预签名信息。
     */
    default UploadPartInfo presignUploadPart(String key, String uploadId, int partNumber, Duration ttl) {
        throw new UnsupportedOperationException("不支持分片直传");
    }

    /**
     * 完成分片上传。
     */
    default void completeMultipartUpload(String key, String uploadId, List<CompletedUploadPart> parts) {
        throw new UnsupportedOperationException("不支持分片直传");
    }

    /**
     * 终止分片上传（清理未完成的分片）。
     */
    default void abortMultipartUpload(String key, String uploadId) {
        throw new UnsupportedOperationException("不支持分片直传");
    }

    /**
     * 获取对象元数据（用于直传完成后的校验）。对象不存在时应返回 exists=false 的结果。
     */
    default ObjectMetadata headObject(String key) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setExists(exist(key));
        return meta;
    }
}
