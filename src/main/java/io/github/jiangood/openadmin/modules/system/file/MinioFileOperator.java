package io.github.jiangood.openadmin.modules.system.file;

import cn.hutool.core.io.FileUtil;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
public class MinioFileOperator implements FileOperator {


    private final String url;
    private final String accessKey;
    private final String secretKey;

    private final String bucketName;
    private MinioClient client;

    public MinioFileOperator(String url, String accessKey, String secretKey, String bucketName) {
        this.url = url;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;

        this.initClient();
    }

    public void initClient() {
        this.client = MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
    }

    @Override
    public void save(String key, InputStream inputStream) throws Exception {
        PutObjectArgs arg = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(key)
                .stream(inputStream, inputStream.available(), -1)
                .build();

        this.client.putObject(arg);

    }

    @Override
    public void saveFile(String key, File file) throws Exception {
        try (InputStream is = new FileInputStream(file)) {
            this.save(key, is);
        }
    }

    @Override
    public InputStream getFileStream(String key) throws Exception {

        GetObjectResponse response = client.getObject(GetObjectArgs.builder().bucket(bucketName).object(key).build());
        return response;
    }

    @Override
    public void downloadFile(String key, File target) throws Exception {
        GetObjectResponse response = client.getObject(GetObjectArgs.builder().bucket(bucketName).object(key).build());

        FileUtil.writeFromStream(response, target, true);
    }

    @Override
    public void delete(String key) throws Exception {
        client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(key).build());
    }

    @Override
    public boolean exist(String key) {
        try {
            StatObjectResponse resp = client.statObject(StatObjectArgs.builder().bucket(bucketName).object(key).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
