package io.github.jiangood.openadmin.modules.system.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.spi.CompletedUploadPart;
import io.github.jiangood.openadmin.framework.spi.DirectUploadInfo;
import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.framework.spi.ObjectMetadata;
import io.github.jiangood.openadmin.framework.spi.UploadPartInfo;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
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
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * S3 协议对象存储实现（AWS SDK v2），可对接 AWS S3、RustFS、阿里云 OSS、腾讯云 COS、Ceph 等。
 */
@Slf4j
public class S3FileOperator implements FileOperator, AutoCloseable {

    private static final String DEFAULT_REGION = "us-east-1";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3FileOperator(String endpoint, String region, String accessKey, String secretKey,
                          String bucketName, boolean pathStyleAccess) {
        if (StrUtil.isBlank(bucketName)) {
            throw new IllegalArgumentException("sys.file.s3.bucket-name 不能为空");
        }
        log.info("S3 对象存储地址: {}, region: {}, bucket: {}", endpoint, region, bucketName);
        this.bucketName = bucketName;
        Region resolvedRegion = Region.of(StrUtil.blankToDefault(region, DEFAULT_REGION).trim());
        AwsCredentialsProvider credentialsProvider = resolveCredentials(accessKey, secretKey);
        URI endpointUri = StrUtil.isNotBlank(endpoint) ? URI.create(normalizeEndpoint(endpoint)) : null;
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyleAccess)
                .build();

        this.s3Client = buildClient(resolvedRegion, credentialsProvider, endpointUri, serviceConfiguration);
        this.s3Presigner = buildPresigner(resolvedRegion, credentialsProvider, endpointUri, serviceConfiguration);
    }

    private static AwsCredentialsProvider resolveCredentials(String accessKey, String secretKey) {
        boolean hasAccessKey = StrUtil.isNotBlank(accessKey);
        boolean hasSecretKey = StrUtil.isNotBlank(secretKey);
        if (hasAccessKey && hasSecretKey) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey.trim(), secretKey.trim()));
        }
        if (hasAccessKey || hasSecretKey) {
            log.warn("S3 access-key / secret-key 只配置了一个，将忽略并使用默认凭证链");
        }
        // 未配置密钥时走默认凭证链：环境变量 / IAM Role / 实例角色
        return DefaultCredentialsProvider.create();
    }

    private static S3Client buildClient(Region region, AwsCredentialsProvider credentialsProvider,
                                        URI endpointUri, S3Configuration serviceConfiguration) {
        S3ClientBuilder builder = S3Client.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .region(region)
                .credentialsProvider(credentialsProvider)
                // SDK 2.30+ 默认开启完整性校验（aws-chunked），部分第三方 S3 兼容存储不识别，按需关闭
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .serviceConfiguration(serviceConfiguration);

        if (endpointUri != null) {
            builder.endpointOverride(endpointUri);
        }
        return builder.build();
    }

    private static S3Presigner buildPresigner(Region region, AwsCredentialsProvider credentialsProvider,
                                              URI endpointUri, S3Configuration serviceConfiguration) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration);
        if (endpointUri != null) {
            builder.endpointOverride(endpointUri);
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

    private static Map<String, String> flattenHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            if (e.getKey() == null || e.getValue() == null || e.getValue().isEmpty()) {
                continue;
            }
            map.put(e.getKey(), String.join(",", e.getValue()));
        }
        return map;
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
    public boolean supportsPresignedGet() {
        return true;
    }

    @Override
    public String presignGetUrl(String key, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofHours(1);
        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }

    @Override
    public boolean supportsDirectUpload() {
        return true;
    }

    @Override
    public DirectUploadInfo presignPut(String key, String contentType, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofMinutes(30);
        }
        PutObjectRequest.Builder reqBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key);
        if (StrUtil.isNotBlank(contentType)) {
            reqBuilder.contentType(contentType.trim());
        }
        PutObjectRequest putObjectRequest = reqBuilder.build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .putObjectRequest(putObjectRequest)
                .build();
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        DirectUploadInfo info = new DirectUploadInfo();
        info.setUrl(presigned.url().toString());
        info.setMethod("PUT");
        info.setHeaders(flattenHeaders(presigned.signedHeaders()));
        info.setExpiresAt(Instant.now().plus(ttl));
        return info;
    }

    @Override
    public String initMultipartUpload(String key, String contentType) {
        CreateMultipartUploadRequest.Builder reqBuilder = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key);
        if (StrUtil.isNotBlank(contentType)) {
            reqBuilder.contentType(contentType.trim());
        }
        CreateMultipartUploadResponse resp = s3Client.createMultipartUpload(reqBuilder.build());
        return resp.uploadId();
    }

    @Override
    public UploadPartInfo presignUploadPart(String key, String uploadId, int partNumber, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofMinutes(30);
        }
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();
        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(ttl)
                .uploadPartRequest(uploadPartRequest)
                .build();
        PresignedUploadPartRequest presigned = s3Presigner.presignUploadPart(presignRequest);

        UploadPartInfo info = new UploadPartInfo();
        info.setPartNumber(partNumber);
        info.setUrl(presigned.url().toString());
        info.setMethod("PUT");
        info.setHeaders(flattenHeaders(presigned.signedHeaders()));
        info.setExpiresAt(Instant.now().plus(ttl));
        return info;
    }

    @Override
    public void completeMultipartUpload(String key, String uploadId, List<CompletedUploadPart> parts) {
        List<CompletedPart> completedParts = parts.stream()
                .map(p -> CompletedPart.builder()
                        .partNumber(p.getPartNumber())
                        .eTag(p.getEtag())
                        .build())
                .toList();
        CompletedMultipartUpload completed = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();
        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(completed)
                .build();
        s3Client.completeMultipartUpload(request);
    }

    @Override
    public void abortMultipartUpload(String key, String uploadId) {
        try {
            AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .uploadId(uploadId)
                    .build();
            s3Client.abortMultipartUpload(request);
        } catch (SdkException e) {
            log.warn("终止 S3 分片上传失败: key={}, uploadId={}, error={}", key, uploadId, e.getMessage());
        }
    }

    @Override
    public ObjectMetadata headObject(String key) {
        ObjectMetadata meta = new ObjectMetadata();
        try {
            HeadObjectResponse resp = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            meta.setExists(true);
            meta.setContentLength(resp.contentLength() == null ? 0 : resp.contentLength());
            meta.setContentType(resp.contentType());
            meta.setEtag(resp.eTag());
            return meta;
        } catch (NoSuchKeyException e) {
            meta.setExists(false);
            return meta;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                meta.setExists(false);
                return meta;
            }
            throw e;
        }
    }

    @Override
    public S3Client getClient() {
        return s3Client;
    }

    @Override
    public void close() {
        try {
            s3Presigner.close();
        } finally {
            s3Client.close();
        }
    }
}
