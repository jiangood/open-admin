package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.modules.system.file.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.MinioFileOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Slf4j
@Configuration
public class FileConfig {

    @Bean
    @Primary
    public FileOperator fileOperator(SystemProperties systemProperties) {
        String storeType = systemProperties.getFile().getStoreType();
        if (storeType.equals("local")) {
            return localFileOperator(systemProperties);
        }
        if (storeType.equals("minio")) {
            return minioFileOperator(systemProperties);
        }
        throw new IllegalArgumentException("sys.file.store-type " + storeType + " 不存在");
    }

        private FileOperator localFileOperator(SystemProperties systemProperties) {
        log.info("本地文件模式");
        return new LocalFileOperator(systemProperties.getFile().getUploadPath());
    }

    private FileOperator minioFileOperator(SystemProperties systemProperties) {
        SystemProperties.FileStorage.Minio minio = systemProperties.getFile().getMinio();
        log.info("配置文件服务为minio模式");
        return new MinioFileOperator(minio.getUrl(), minio.getAccessKey(), minio.getSecretKey(), minio.getBucketName());
    }
}
