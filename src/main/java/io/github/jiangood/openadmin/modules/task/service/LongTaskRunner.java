package io.github.jiangood.openadmin.modules.task.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.modules.logviewer.util.TaskLog;
import io.github.jiangood.openadmin.modules.task.registry.TaskLogRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 兼容旧版长任务入口。
 *
 * @deprecated 请使用 {@link SysTaskExecutor} 的 {@code submit(...)} 方法。
 */
@Deprecated
@Service
@Slf4j
public class LongTaskRunner {

    @Resource
    private SysTaskLogService taskLogService;

    @Resource
    private TaskLogRegistry taskLogRegistry;

    /**
     * @deprecated 使用 {@link SysTaskExecutor.TaskWork}
     */
    @Deprecated
    @FunctionalInterface
    public interface TaskWork {
        void run() throws Exception;
    }

    /**
     * @deprecated 使用 {@link SysTaskExecutor#submit(String, String, SysTaskExecutor.TaskWork)} 或带权限/指定 logId 的重载
     */
    @Deprecated
    public String start(String type, String title, String permission) {
        String logId = type + "-" + RandomUtil.randomString(16);
        String perm = (permission == null || permission.isBlank()) ? null : permission.trim();
        taskLogService.start(logId, type, title, perm, taskLogService.currentTriggerUser());
        taskLogRegistry.start(logId);
        return logId;
    }

    /**
     * @deprecated 使用 {@link SysTaskExecutor} 统一提交执行
     */
    @Deprecated
    public void execute(String logId, TaskWork work) {
        taskLogRegistry.start(logId);
        TaskLog.bind("task/" + logId);
        boolean success = false;
        String error = null;
        try {
            work.run();
            success = true;
        } catch (Exception e) {
            error = e.getMessage();
            TaskLog.error("任务失败: {}", error, e);
        } finally {
            try {
                taskLogService.finish(logId, success, error);
            } catch (Exception e) {
                log.warn("写入任务记录失败: logId={} {}", logId, e.getMessage());
            }
            TaskLog.unbind();
            taskLogRegistry.finish(logId);
        }
    }
}
