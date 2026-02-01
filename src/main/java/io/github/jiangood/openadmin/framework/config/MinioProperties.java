package io.github.jiangood.openadmin.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {
    /**
     * 是否启用minio存储服务		是	不启用的情况，使用的是本地存储
     */
    Boolean enable = false;

    String url;

    /***
     * minio服务accessKey
     */
    String accessKey;


    /***
     * minio服务密钥
     */
    String secretKey;

    /**
     * minio服务存储桶
     */
    String bucketName;

}
