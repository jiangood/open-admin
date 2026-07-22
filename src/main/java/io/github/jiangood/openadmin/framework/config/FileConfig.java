package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.modules.system.file.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.S3FileOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 文件存储配置。
 * <p>store-type = custom 时，需自行注册 FileOperator bean：</p>
 * <pre>{@code
 * @Bean
 * @Primary
 * public FileOperator myFileOperator() {
 *     return new MyCustomFileOperator();
 * }
 * }</pre>
 */
@Slf4j
@Configuration
public class FileConfig {

    @Bean
    @ConditionalOnMissingBean(FileOperator.class)
    public FileOperator fileOperator(SystemProperties sp) {
        var file = sp.getFile();
        var storeType = file.getStoreType();
        log.info("文件存储模式: {}", storeType);
        if (storeType == SystemProperties.FileStorage.StoreType.local) {
            return new LocalFileOperator(file.getUploadPath());
        }
        if (storeType == SystemProperties.FileStorage.StoreType.s3) {
            var s3 = file.getS3();
            return new S3FileOperator(s3.getEndpoint(), s3.getRegion(), s3.getAccessKey(), s3.getSecretKey(), s3.getBucketName(), s3.getPathStyleAccess());
        }
        if (storeType == SystemProperties.FileStorage.StoreType.custom) {
            throw new IllegalArgumentException("sys.file.store-type 为 custom，但未发现 FileOperator 的 bean。请在项目中注册自定义 FileOperator 实现");
        }
        throw new IllegalArgumentException("不支持的存储类型: " + storeType);
    }
}
