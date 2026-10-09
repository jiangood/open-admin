package io.github.jiangood.openadmin.modules.logviewer.service;

import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 控制器 {@code @GetMapping("{*key}")} 捕获到的路径带前导 "/"，
 * 这里验证服务能正确处理该前导斜杠，同时仍拦截路径穿越。
 */
class FileLogServiceTest {

    @TempDir
    Path logDir;

    private FileLogService service;

    @BeforeEach
    void setUp() throws Exception {
        FileLogConfig config = new FileLogConfig();
        Field logPath = FileLogConfig.class.getDeclaredField("logPath");
        logPath.setAccessible(true);
        logPath.set(config, logDir.toString());

        service = new FileLogService();
        Field field = FileLogService.class.getDeclaredField("fileLogConfig");
        field.setAccessible(true);
        field.set(service, config);
    }

    @Test
    void toleratesLeadingSlashFromWildcardPathVariable() throws Exception {
        Files.writeString(logDir.resolve("abc.log"), "hello");

        assertEquals("hello", service.readLogContent("/abc"));
        assertEquals("hello", service.readLogContent("abc"));
    }

    @Test
    void supportsMultiSegmentKey() throws Exception {
        Files.createDirectories(logDir.resolve("job"));
        Files.writeString(logDir.resolve("job/123.log"), "line1\nline2");

        assertEquals("line1\nline2", service.readLogContent("/job/123"));
    }

    @Test
    void rejectsPathTraversal() {
        assertThrows(IllegalArgumentException.class, () -> service.readLogContent("/../secret"));
        assertThrows(IllegalArgumentException.class, () -> service.readLogContent("/.."));
        assertThrows(IllegalArgumentException.class, () -> service.readLogContent("/a//b"));
        assertThrows(IllegalArgumentException.class, () -> service.readLogContent("/"));
    }
}
