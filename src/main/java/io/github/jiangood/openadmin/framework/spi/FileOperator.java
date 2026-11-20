package io.github.jiangood.openadmin.framework.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

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
}
