package io.github.jiangood.openadmin.modules.logviewer.util;

import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FileLogTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void bind_shouldSetKey() {
        FileLog.bind("job/1");

        assertEquals("job/1", FileLog.currentKey());
    }

    @Test
    void unbind_shouldOnlyRemoveOwnKey() {
        MDC.put("traceId", "t-1");
        FileLog.bind("job/1");

        FileLog.unbind();

        assertNull(FileLog.currentKey());
        // 不能误清其它 MDC 字段（旧 clear() 会 MDC.clear()，导致 traceId 丢失）
        assertEquals("t-1", MDC.get("traceId"));
    }

    @Test
    void bind_shouldRestoreOuterBindingWhenNested() {
        FileLog.bind("job/outer");
        String previous = FileLog.bind("order/inner");

        assertEquals("order/inner", FileLog.currentKey());

        FileLog.unbind(previous);

        // 内层结束后恢复外层绑定，避免任务里调用带文件日志的方法后日志串到 DEFAULT
        assertEquals("job/outer", FileLog.currentKey());
    }

    @Test
    void unbindWithNullPrevious_shouldClearBinding() {
        String previous = FileLog.bind("job/1");

        FileLog.unbind(previous);

        assertNull(FileLog.currentKey());
    }

    @Test
    void logger_shouldUseSharedFileLoggerName() {
        assertNotNull(FileLog.logger());
        assertEquals(FileLogConfig.LOGGER_NAME, FileLog.logger().getName());
    }
}
