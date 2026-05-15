package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.modules.system.file.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.MinioFileOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;


@Slf4j
@Configuration
public class FileConfig {

    @Bean("local")
    public FileOperator localFileOperator(SystemProperties systemProperties) {
        log.info("本地文件模式");
        return new LocalFileOperator(systemProperties.getFile().getUploadPath());
    }

    @Bean("minio")
    public FileOperator minioFileOperator(SystemProperties systemProperties) {
        SystemProperties.FileStorage.Minio minio = systemProperties.getFile().getMinio();
        log.info("配置文件服务为minio模式");
        return new MinioFileOperator(minio.getUrl(), minio.getAccessKey(), minio.getSecretKey(), minio.getBucketName());
    }

    @Bean
    @Primary
    public FileOperator fileOperator(Map<String, FileOperator> operators, SystemProperties systemProperties) {
        String storeType = systemProperties.getFile().getStoreType();
        FileOperator op = operators.get(storeType);
        if (op == null) {
            log.warn("sys.file.store-type '{}' 对应的 FileOperator 不存在，使用 local", storeType);
            op = operators.get("local");
        }
        return op;
    }
}
