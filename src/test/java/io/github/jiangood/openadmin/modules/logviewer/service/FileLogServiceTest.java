package io.github.jiangood.openadmin.modules.logviewer.service;

import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import io.github.jiangood.openadmin.modules.logviewer.dto.LogChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 控制器 {@code @GetMapping("{*key}")} 捕获到的路径带前导 "/"，
 * 这里验证服务能正确处理该前导斜杠，同时仍拦截路径穿越；并覆盖增量读取。
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

    @Test
    void readChunk_shouldReturnWholeFileAndEof() throws Exception {
        Files.writeString(logDir.resolve("abc.log"), "hello");

        LogChunk chunk = service.readChunk("abc", 0, 0);

        assertEquals("hello", chunk.getContent());
        assertEquals(5, chunk.getNextOffset());
        assertTrue(chunk.isEof());
    }

    @Test
    void readChunk_shouldOnlyReturnNewContentFromOffset() throws Exception {
        Files.writeString(logDir.resolve("abc.log"), "hello world");

        LogChunk chunk = service.readChunk("abc", 6, 0);

        assertEquals("world", chunk.getContent());
        assertEquals(11, chunk.getNextOffset());
        assertTrue(chunk.isEof());
    }

    @Test
    void readChunk_shouldBeEmptyWhenNoNewContent() throws Exception {
        Files.writeString(logDir.resolve("abc.log"), "hello");

        LogChunk chunk = service.readChunk("abc", 5, 0);

        assertEquals("", chunk.getContent());
        assertEquals(5, chunk.getNextOffset());
        assertTrue(chunk.isEof());
    }

    @Test
    void readChunk_shouldRestartWhenFileShrunk() throws Exception {
        Files.writeString(logDir.resolve("abc.log"), "abc");

        LogChunk chunk = service.readChunk("abc", 100, 0);

        assertEquals("abc", chunk.getContent());
        assertEquals(3, chunk.getNextOffset());
        assertTrue(chunk.isEof());
    }

    @Test
    void readChunk_shouldNotSplitMultiByteCharacter() throws Exception {
        String text = "中文日志";
        Files.write(logDir.resolve("abc.log"), text.getBytes(StandardCharsets.UTF_8));

        // 每个汉字 3 字节：限制为 4 字节时只能返回第一个完整汉字
        LogChunk first = service.readChunk("abc", 0, 4);
        assertEquals("中", first.getContent());
        assertEquals(3, first.getNextOffset());
        assertFalse(first.isEof());

        LogChunk second = service.readChunk("abc", first.getNextOffset(), 4);
        assertEquals("文", second.getContent());
        assertEquals(6, second.getNextOffset());
        assertFalse(second.isEof());
    }

    @Test
    void readChunk_shouldHandleMissingFile() throws Exception {
        LogChunk chunk = service.readChunk("not-exists", 0, 0);

        assertEquals("", chunk.getContent());
        assertTrue(chunk.isEof());
    }

    @Test
    void readChunk_shouldRejectPathTraversal() {
        assertThrows(IllegalArgumentException.class, () -> service.readChunk("/../secret", 0, 0));
    }
}
