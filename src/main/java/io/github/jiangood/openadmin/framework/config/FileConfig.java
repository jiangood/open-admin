package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.S3FileOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FileConfig {

    @Bean
    @ConditionalOnMissingBean(FileOperator.class)
    public static FileOperator fileOperator(SystemProperties sp) {
        var file = sp.getFile();
        switch (file.getStoreType()) {
            case LOCAL:
                return new LocalFileOperator(file.getUploadPath());
            case S3: {
                var s3 = file.getS3();
                return new S3FileOperator(s3.getEndpoint(), s3.getRegion(), s3.getAccessKey(), s3.getSecretKey(), s3.getBucketName(), s3.getPathStyleAccess());
            }
            default:
                throw new IllegalArgumentException("store-type 为 custom 时请注册 @Bean @Primary FileOperator");
        }
    }
}
