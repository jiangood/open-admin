package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.MinioFileOperator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FileConfig {

    FileConfig() {
    }

    @Bean
    public FileOperator fileOperator(SystemProperties sp) {
        var file = sp.getFile();
        return switch (file.getStoreType()) {
            case LOCAL -> new LocalFileOperator(file.getUploadPath());
            case MINIO -> {
                var minio = file.getMinio();
                yield new MinioFileOperator(minio.getEndpoint(), minio.getAccessKey(), minio.getSecretKey(), minio.getBucketName());
            }
        };
    }
}
