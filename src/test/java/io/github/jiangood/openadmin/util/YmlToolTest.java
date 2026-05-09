package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class YmlToolTest {

    // 测试用的配置类
    static class TestConfig {
        private String name;
        private int port;
        private boolean enabled;
        private DatabaseConfig database;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public DatabaseConfig getDatabase() {
            return database;
        }

        public void setDatabase(DatabaseConfig database) {
            this.database = database;
        }
    }

    // 嵌套的配置类
    static class DatabaseConfig {
        private String url;
        private String username;
        private String password;

        // Getters and setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Test
    void testParseYmlWithPrefix() {
        // 测试带有前缀的 YAML 解析
        String yml = "" +
                "app:\n" +
                "  name: test-app\n" +
                "  port: 8080\n" +
                "  enabled: true\n" +
                "  database:\n" +
                "    url: jdbc:mysql://localhost:3306/test\n" +
                "    username: root\n" +
                "    password: password\n";

        TestConfig config = YmlTool.parseYml(yml, TestConfig.class, "app");
        assertNotNull(config);
        assertEquals("test-app", config.getName());
        assertEquals(8080, config.getPort());
        assertTrue(config.isEnabled());
        assertNotNull(config.getDatabase());
        assertEquals("jdbc:mysql://localhost:3306/test", config.getDatabase().getUrl());
        assertEquals("root", config.getDatabase().getUsername());
        assertEquals("password", config.getDatabase().getPassword());
    }

    @Test
    void testParseYmlWithoutPrefix() {
        // 测试不带前缀的 YAML 解析
        String yml = "" +
                "name: test-app\n" +
                "port: 8080\n" +
                "enabled: true\n" +
                "database:\n" +
                "  url: jdbc:mysql://localhost:3306/test\n" +
                "  username: root\n" +
                "  password: password\n";

        TestConfig config = YmlTool.parseYml(yml, TestConfig.class, "");
        assertNotNull(config);
        assertEquals("test-app", config.getName());
        assertEquals(8080, config.getPort());
        assertTrue(config.isEnabled());
        assertNotNull(config.getDatabase());
        assertEquals("jdbc:mysql://localhost:3306/test", config.getDatabase().getUrl());
        assertEquals("root", config.getDatabase().getUsername());
        assertEquals("password", config.getDatabase().getPassword());
    }

    @Test
    void testParseYmlWithInputStream() {
        // 测试使用 InputStream 解析 YAML
        String yml = "" +
                "app:\n" +
                "  name: test-app\n" +
                "  port: 8080\n" +
                "  enabled: true\n";

        try (InputStream is = new ByteArrayInputStream(yml.getBytes())) {
            TestConfig config = YmlTool.parseYml(is, TestConfig.class, "app");
            assertNotNull(config);
            assertEquals("test-app", config.getName());
            assertEquals(8080, config.getPort());
            assertTrue(config.isEnabled());
        } catch (Exception e) {
            fail("解析 YAML 时出错: " + e.getMessage());
        }
    }

    @Test
    void testParseYmlWithEmptyYml() {
        // 测试解析空的 YAML，空字符串不是有效的YAML，应该抛出异常
        String emptyYml = "";
        assertThrows(Exception.class, () -> {
            YmlTool.parseYml(emptyYml, TestConfig.class, "");
        });
    }

    @Test
    void testParseYmlWithInvalidYml() {
        // 测试解析无效的 YAML
        String invalidYml = "name: test-app\nport: not-a-number\n";
        
        // 这里应该会抛出异常，因为 port 不是一个有效的数字
        assertThrows(Exception.class, () -> {
            YmlTool.parseYml(invalidYml, TestConfig.class, "");
        });
    }

}
