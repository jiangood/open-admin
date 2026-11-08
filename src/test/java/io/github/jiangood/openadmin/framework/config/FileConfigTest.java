package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.S3FileOperator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * 文件存储配置分发测试：验证 FileConfig 根据 store-type 选择正确的 FileOperator 实现。
 */
class FileConfigTest {

    @Test
    void shouldCreateLocalOperatorByDefault() {
        SystemProperties sp = new SystemProperties();

        FileOperator operator = new FileConfig().fileOperator(sp);

        assertInstanceOf(LocalFileOperator.class, operator);
    }

    @Test
    void shouldCreateS3OperatorWhenStoreTypeIsS3() {
        SystemProperties sp = new SystemProperties();
        sp.getFile().setStoreType(SystemProperties.FileStorage.StoreType.S3);
        sp.getFile().getS3().setEndpoint("http://localhost:9000");
        sp.getFile().getS3().setAccessKey("access");
        sp.getFile().getS3().setSecretKey("secret");
        sp.getFile().getS3().setBucketName("bucket");

        FileOperator operator = new FileConfig().fileOperator(sp);

        assertInstanceOf(S3FileOperator.class, operator);
    }
}
