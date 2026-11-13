package io.github.jiangood.openadmin.modules.logviewer.util;

import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

class TaskLogTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void bind_shouldSetKey() {
        TaskLog.bind("job/1");

        assertEquals("job/1", TaskLog.currentKey());
    }

    @Test
    void unbind_shouldOnlyRemoveOwnKey() {
        MDC.put("traceId", "t-1");
        TaskLog.bind("job/1");

        TaskLog.unbind();

        assertNull(TaskLog.currentKey());
        // 不能误清其它 MDC 字段
        assertEquals("t-1", MDC.get("traceId"));
    }

    @Test
    void bind_shouldRestoreOuterBindingWhenNested() {
        TaskLog.bind("job/outer");
        String previous = TaskLog.bind("order/inner");

        assertEquals("order/inner", TaskLog.currentKey());

        TaskLog.unbind(previous);

        // 内层结束后恢复外层绑定，避免任务里调用带文件日志的方法后日志串到 DEFAULT
        assertEquals("job/outer", TaskLog.currentKey());
    }

    @Test
    void unbindWithNullPrevious_shouldClearBinding() {
        String previous = TaskLog.bind("job/1");

        TaskLog.unbind(previous);

        assertNull(TaskLog.currentKey());
    }

    @Test
    void logger_shouldUseSharedFileLoggerName() {
        assertNotNull(TaskLog.logger());
        assertEquals(FileLogConfig.LOGGER_NAME, TaskLog.logger().getName());
    }
}
