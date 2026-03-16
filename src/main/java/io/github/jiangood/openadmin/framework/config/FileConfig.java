package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.modules.system.file.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.MinioFileOperator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class FileConfig {

    @Resource
    MinioProperties minioProperties;


    @Resource
    SystemProperties systemProperties;

    @Bean
    public FileOperator fileOperator() {
        if (minioProperties.getEnable()) {
            log.info("配置文件服务为minio模式");
            return new MinioFileOperator(minioProperties.getUrl(), minioProperties.getAccessKey(), minioProperties.getSecretKey(), minioProperties.getBucketName());
        }
        log.info("本地文件模式");
        return new LocalFileOperator(systemProperties.getFileUploadPath());
    }

}
