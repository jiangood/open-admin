package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.MinioFileOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class FileConfig {

    private FileConfig() {
    }

    @Bean
    @ConditionalOnMissingBean(FileOperator.class)
    public static FileOperator fileOperator(SystemProperties sp) {
        var file = sp.getFile();
        switch (file.getStoreType()) {
            case LOCAL:
                return new LocalFileOperator(file.getUploadPath());
            case MINIO: {
                var minio = file.getMinio();
                return new MinioFileOperator(minio.getEndpoint(), minio.getAccessKey(), minio.getSecretKey(), minio.getBucketName());
            }
            default:
                throw new IllegalArgumentException("store-type 为 custom 时请注册 @Bean @Primary FileOperator");
        }
    }
}
