package io.github.jiangood.openadmin.modules.config.service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.config.dto.SystemParamView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        List<SystemParamView> items = new SystemParamService(properties, environment, provider(null)).items();

        assertEquals("sys.start-time", items.get(0).key());
        assertTrue(valueOf(items, "sys.start-time")
                .matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}（已运行 .+）"));
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
        List<SystemParamView> items = new SystemParamService(new SystemProperties(), new MockEnvironment(), provider(null)).items();

        assertEquals("1", valueOf(items, "sys.maximum-sessions"));
        assertEquals("阻止新登录（旧会话继续有效）", valueOf(items, "sys.max-sessions-prevents-login"));
        assertEquals("180 分钟", valueOf(items, "sys.session-idle-time"));
        assertEquals("本地存储", valueOf(items, "sys.file.store-type"));
        assertEquals("-", valueOf(items, "logging.file.path"));
        assertEquals("-", valueOf(items, "spring.datasource.url"));
        assertTrue(items.stream().noneMatch(item -> item.key().startsWith("sys.job-")));
        assertTrue(items.stream().noneMatch(item -> item.key().contains("secret")
                || item.key().contains("password")
                || item.key().contains("access-key")));
    }

    @Test
    void exposesJdbcUrlWithoutPassword() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.datasource.url",
                "jdbc:mysql://root:123456@127.0.0.1:3306/open-admin?password=abc&useSSL=false");

        List<SystemParamView> items = new SystemParamService(new SystemProperties(), environment, provider(null)).items();

        assertEquals("jdbc:mysql://root:***@127.0.0.1:3306/open-admin?password=***&useSSL=false",
                valueOf(items, "spring.datasource.url"));
        assertTrue(items.stream().noneMatch(item -> "spring.datasource.hikari".equals(item.key())));
    }

    @Test
    void exposesHikariPoolUsage() {
        HikariDataSource dataSource = mock(HikariDataSource.class);
        HikariPoolMXBean pool = mock(HikariPoolMXBean.class);
        when(dataSource.getJdbcUrl()).thenReturn("jdbc:h2:file:/data/db/open-admin");
        when(dataSource.getMaximumPoolSize()).thenReturn(10);
        when(dataSource.getHikariPoolMXBean()).thenReturn(pool);
        when(pool.getActiveConnections()).thenReturn(2);
        when(pool.getIdleConnections()).thenReturn(8);
        when(pool.getThreadsAwaitingConnection()).thenReturn(0);

        List<SystemParamView> items = new SystemParamService(new SystemProperties(), new MockEnvironment(), provider(dataSource)).items();

        assertEquals("jdbc:h2:file:/data/db/open-admin", valueOf(items, "spring.datasource.url"));
        assertEquals("活动 2 / 空闲 8 / 上限 10 / 等待 0", valueOf(items, "spring.datasource.hikari"));
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<DataSource> provider(DataSource dataSource) {
        ObjectProvider<DataSource> provider = mock(ObjectProvider.class);
        when(provider.getIfUnique()).thenReturn(dataSource);
        return provider;
    }

    private String valueOf(List<SystemParamView> items, String key) {
        return items.stream()
                .filter(item -> key.equals(item.key()))
                .map(SystemParamView::value)
                .findFirst()
                .orElseThrow();
    }
}
