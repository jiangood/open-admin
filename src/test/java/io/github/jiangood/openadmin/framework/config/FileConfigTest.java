package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.modules.system.file.LocalFileOperator;
import io.github.jiangood.openadmin.modules.system.file.MinioFileOperator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 文件存储配置分发测试：验证 FileConfig 根据 store-type 选择正确的 FileOperator 实现。
 */
class FileConfigTest {

    @Test
    void shouldCreateLocalOperatorByDefault() {
        SystemProperties sp = new SystemProperties();

        FileOperator operator = FileConfig.fileOperator(sp);

        assertInstanceOf(LocalFileOperator.class, operator);
    }

    @Test
    void shouldCreateMinioOperatorWhenStoreTypeIsMinio() {
        SystemProperties sp = new SystemProperties();
        sp.getFile().setStoreType(SystemProperties.FileStorage.StoreType.MINIO);
        sp.getFile().getMinio().setEndpoint("http://localhost:9000");
        sp.getFile().getMinio().setAccessKey("access");
        sp.getFile().getMinio().setSecretKey("secret");
        sp.getFile().getMinio().setBucketName("bucket");

        FileOperator operator = FileConfig.fileOperator(sp);

        assertInstanceOf(MinioFileOperator.class, operator);
    }

    @Test
    void shouldThrowWhenStoreTypeIsCustom() {
        SystemProperties sp = new SystemProperties();
        sp.getFile().setStoreType(SystemProperties.FileStorage.StoreType.CUSTOM);

        assertThrows(IllegalArgumentException.class, () -> FileConfig.fileOperator(sp));
    }
}