package io.github.jiangood.openadmin.modules.system.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.spi.FileOperator;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * S3 协议对象存储实现（AWS SDK v2），可对接 AWS S3、RustFS、阿里云 OSS、腾讯云 COS、Ceph 等。
 */
@Slf4j
public class S3FileOperator implements FileOperator, AutoCloseable {

    private static final String DEFAULT_REGION = "us-east-1";

    private final S3Client s3Client;
    private final String bucketName;

    public S3FileOperator(String endpoint, String region, String accessKey, String secretKey,
                          String bucketName, boolean pathStyleAccess) {
        if (StrUtil.isBlank(bucketName)) {
            throw new IllegalArgumentException("sys.file.s3.bucket-name 不能为空");
        }
        log.info("S3 对象存储地址: {}, region: {}, bucket: {}", endpoint, region, bucketName);
        this.bucketName = bucketName;
        this.s3Client = buildClient(endpoint, region, accessKey, secretKey, pathStyleAccess);
    }

    private static S3Client buildClient(String endpoint, String region, String accessKey, String secretKey,
                                        boolean pathStyleAccess) {
        S3ClientBuilder builder = S3Client.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .region(Region.of(StrUtil.blankToDefault(region, DEFAULT_REGION).trim()))
                // SDK 2.30+ 默认开启完整性校验（aws-chunked），部分第三方 S3 兼容存储不识别，按需关闭
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyleAccess)
                        .build());

        if (StrUtil.isNotBlank(endpoint)) {
            builder.endpointOverride(URI.create(normalizeEndpoint(endpoint)));
        }

        boolean hasAccessKey = StrUtil.isNotBlank(accessKey);
        boolean hasSecretKey = StrUtil.isNotBlank(secretKey);
        if (hasAccessKey && hasSecretKey) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey.trim(), secretKey.trim())));
        } else {
            if (hasAccessKey || hasSecretKey) {
                log.warn("S3 access-key / secret-key 只配置了一个，将忽略并使用默认凭证链");
            }
            // 未配置密钥时走默认凭证链：环境变量 / IAM Role / 实例角色
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }

    private static String normalizeEndpoint(String endpoint) {
        String value = endpoint.trim();
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "https://" + value;
        }
        return value;
    }

    @Override
    public void save(String key, InputStream inputStream) throws IOException {
        long length = lengthOf(inputStream);
        if (length >= 0) {
            putObject(key, RequestBody.fromInputStream(inputStream, length));
            return;
        }

        // S3 协议要求请求体长度已知，未知长度（如网络流）先落临时文件再上传
        File temp = File.createTempFile("open-admin-s3-", ".tmp");
        try {
            FileUtil.writeFromStream(inputStream, temp);
            putObject(key, RequestBody.fromFile(temp));
        } finally {
            FileUtil.del(temp);
        }
    }

    private long lengthOf(InputStream inputStream) {
        if (inputStream instanceof FileInputStream fileInputStream) {
            try {
                // 注意用 size - position，流可能已被读取过一部分
                var channel = fileInputStream.getChannel();
                return channel.size() - channel.position();
            } catch (IOException e) {
                log.debug("读取文件流大小失败，将使用临时文件中转", e);
            }
        }
        return -1;
    }

    @Override
    public void saveFile(String key, File file) throws IOException {
        putObject(key, RequestBody.fromFile(file));
    }

    private void putObject(String key, RequestBody body) throws IOException {
        try {
            s3Client.putObject(b -> b.bucket(bucketName).key(key), body);
        } catch (SdkException e) {
            throw new IOException("上传文件到 S3 失败: " + key, e);
        }
    }

    @Override
    public InputStream getFileStream(String key) throws IOException {
        try {
            return s3Client.getObject(b -> b.bucket(bucketName).key(key));
        } catch (SdkException e) {
            throw new IOException("从 S3 读取文件失败: " + key, e);
        }
    }

    @Override
    public void downloadFile(String key, File target) throws IOException {
        try (InputStream is = getFileStream(key)) {
            FileUtil.writeFromStream(is, target);
        }
    }

    @Override
    public void delete(String key) throws IOException {
        try {
            s3Client.deleteObject(b -> b.bucket(bucketName).key(key));
        } catch (SdkException e) {
            throw new IOException("从 S3 删除文件失败: " + key, e);
        }
    }

    @Override
    public boolean exist(String key) {
        try {
            s3Client.headObject(b -> b.bucket(bucketName).key(key));
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            log.warn("检查 S3 文件是否存在失败: key={}, error={}", key, e.getMessage());
            return false;
        } catch (SdkException e) {
            log.warn("检查 S3 文件是否存在失败: key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public S3Client getClient() {
        return s3Client;
    }

    @Override
    public void close() {
        s3Client.close();
    }
}
