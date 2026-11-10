package io.github.jiangood.openadmin.modules.config.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.config.dto.SystemParamView;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemParamServiceTest {

    @Test
    void exposesReadonlyStartupConfigWithFormattedValues() {
        SystemProperties properties = new SystemProperties();
        properties.setMaximumSessions(3);
        properties.setMaxSessionsPreventsLogin(false);
        properties.setSessionIdleTime(120);
        properties.setLoginLockMaxAttempts(5);
        properties.setLoginLockMinutes(10);
        properties.setDataFileDir("/data/open-admin");

        SystemProperties.FileStorage file = new SystemProperties.FileStorage();
        file.setStoreType(SystemProperties.FileStorage.StoreType.S3);
        file.setUploadPath("/data/upload");
        file.setCleanTempMinutes(60);
        properties.setFile(file);

        MockEnvironment environment = new MockEnvironment()
                .withProperty("logging.file.path", "/data/logs");

        List<SystemParamView> items = new SystemParamService(properties, environment).items();

        assertEquals("3", valueOf(items, "sys.maximum-sessions"));
        assertEquals("踢出旧会话", valueOf(items, "sys.max-sessions-prevents-login"));
        assertEquals("120 分钟", valueOf(items, "sys.session-idle-time"));
        assertEquals("5 次", valueOf(items, "sys.login-lock-max-attempts"));
        assertEquals("10 分钟", valueOf(items, "sys.login-lock-minutes"));
        assertEquals("S3 对象存储", valueOf(items, "sys.file.store-type"));
        assertEquals("/data/upload", valueOf(items, "sys.file.upload-path"));
        assertEquals("60 分钟", valueOf(items, "sys.file.clean-temp-minutes"));
        assertEquals("/data/open-admin", valueOf(items, "sys.data-file-dir"));
        assertEquals("/data/logs", valueOf(items, "logging.file.path"));
    }

    @Test
    void usesCodeDefaultsAndDoesNotExposeSecrets() {
        List<SystemParamView> items = new SystemParamService(new SystemProperties(), new MockEnvironment()).items();

        assertEquals("1", valueOf(items, "sys.maximum-sessions"));
        assertEquals("阻止新登录（旧会话继续有效）", valueOf(items, "sys.max-sessions-prevents-login"));
        assertEquals("180 分钟", valueOf(items, "sys.session-idle-time"));
        assertEquals("本地存储", valueOf(items, "sys.file.store-type"));
        assertEquals("-", valueOf(items, "logging.file.path"));
        assertTrue(items.stream().noneMatch(item -> item.key().startsWith("sys.job-")));
        assertTrue(items.stream().noneMatch(item -> item.key().contains("secret")
                || item.key().contains("password")
                || item.key().contains("access-key")));
    }

    private String valueOf(List<SystemParamView> items, String key) {
        return items.stream()
                .filter(item -> key.equals(item.key()))
                .map(SystemParamView::value)
                .findFirst()
                .orElseThrow();
    }
}
