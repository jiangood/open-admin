package io.github.jiangood.openadmin.framework.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 锁定 application.yml 的文档优先级约定：{@code spring.config.import} 导入的内容会插在
 * 「声明 import 的那个文档」之后，所以业务/测试配置必须写在 {@code ---} 之后的新文档里，
 * 才能覆盖 {@code application-lib.yml} 的默认值。
 * <p>
 * {@code src/test/resources/application.yml} 里的 {@code sys.file.upload-path} 同时被 lib（派生值）
 * 与本文件定义：只要有人把这段配置挪回第一个文档，这条断言就会失败。
 */
@SpringBootTest
class ConfigImportPrecedenceTest {

    @Autowired
    private Environment environment;

    @Test
    void importIsEffective() {
        assertTrue(environment.getProperty("spring.config.import", "").contains("application-lib.yml"));
        // lib 中独有的配置正常生效
        assertEquals("caffeine", environment.getProperty("spring.cache.type"));
        assertEquals("/data", environment.getProperty("sys.data-dir"));
    }

    @Test
    void laterDocumentOverridesImportedLib() {
        // lib 默认是 ${sys.data-dir}/upload（即 /data/upload），测试配置在 --- 之后覆盖为 /tmp/test-uploads
        assertEquals("/tmp/test-uploads", environment.getProperty("sys.file.upload-path"));
    }
}
