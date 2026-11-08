package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.S3FileOperator;
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
            case S3 -> {
                var s3 = file.getS3();
                yield new S3FileOperator(s3.getEndpoint(), s3.getRegion(), s3.getAccessKey(), s3.getSecretKey(),
                        s3.getBucketName(), s3.isPathStyleAccess());
            }
        };
    }
}
