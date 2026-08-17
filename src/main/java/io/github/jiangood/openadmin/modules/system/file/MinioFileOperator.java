package io.github.jiangood.openadmin.modules.system.file;

import cn.hutool.core.io.FileUtil;
import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class MinioFileOperator implements FileOperator {

    private static final long PART_SIZE = 10L * 1024 * 1024;

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioFileOperator(String endpoint, String accessKey, String secretKey, String bucketName) {
        log.info("MinIO 对象存储地址: {}, bucket: {}", endpoint, bucketName);
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public void save(String key, InputStream inputStream) throws IOException {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .stream(inputStream, -1, PART_SIZE)
                    .build());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("上传文件到 MinIO 失败: " + key, e);
        }
    }

    @Override
    public void saveFile(String key, File file) throws IOException {
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .filename(file.getAbsolutePath())
                    .build());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("上传文件到 MinIO 失败: " + key, e);
        }
    }

    @Override
    public InputStream getFileStream(String key) throws IOException {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .build());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("从 MinIO 读取文件失败: " + key, e);
        }
    }

    @Override
    public void downloadFile(String key, File target) throws IOException {
        try (InputStream is = getFileStream(key)) {
            FileUtil.writeFromStream(is, target, true);
        }
    }

    @Override
    public void delete(String key) throws IOException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .build());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IOException("从 MinIO 删除文件失败: " + key, e);
        }
    }

    @Override
    public boolean exist(String key) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            log.warn("检查 MinIO 文件是否存在失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public MinioClient getClient() {
        return minioClient;
    }
}