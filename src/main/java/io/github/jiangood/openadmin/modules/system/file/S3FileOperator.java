package io.github.jiangood.openadmin.modules.system.file;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

@Slf4j
public class S3FileOperator implements FileOperator {

    private final S3Client s3Client;
    private final String bucketName;

    public S3FileOperator(String endpoint, String region, String accessKey, String secretKey, String bucketName, Boolean pathStyleAccess) {
        this.bucketName = bucketName;

        var credentials = AwsBasicCredentials.create(accessKey, secretKey);
        var s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyleAccess)
                .build();

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(s3Configuration)
                .build();
    }

    @Override
    public void save(String key, InputStream inputStream) throws Exception {
        File tempFile = FileUtil.createTempFile();
        try {
            FileUtil.writeFromStream(inputStream, tempFile, true);
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    RequestBody.fromFile(tempFile));
        } finally {
            FileUtil.del(tempFile);
        }
    }

    @Override
    public void saveFile(String key, File file) throws Exception {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromFile(file));
    }

    @Override
    public InputStream getFileStream(String key) throws Exception {
        return s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    @Override
    public void downloadFile(String key, File target) throws Exception {
        try (InputStream is = getFileStream(key)) {
            FileUtil.writeFromStream(is, target, true);
        }
    }

    @Override
    public void delete(String key) throws Exception {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    @Override
    public boolean exist(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public S3Client getClient() {
        return s3Client;
    }
}
