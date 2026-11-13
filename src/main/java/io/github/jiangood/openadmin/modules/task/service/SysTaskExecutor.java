package io.github.jiangood.openadmin.modules.task.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.modules.logviewer.util.TaskLog;
import io.github.jiangood.openadmin.modules.task.entity.SysTaskLog;
import io.github.jiangood.openadmin.modules.task.registry.TaskLogRegistry;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.concurrent.Executor;
import java.util.regex.Pattern;

/**
 * 后台长任务统一入口：登记任务记录、绑定日志、异步执行并收尾。
 * <p>
 * 对外只暴露两个 {@code submit} 方法：
 * <ul>
 *   <li>最简：{@link #submit(String, String, TaskWork)} —— 自动生成 logId，日志仅登录用户可查看</li>
 *   <li>最全：{@link #submit(String, String, String, String, TaskWork)} —— 可指定 logId 与查看权限</li>
 * </ul>
 * <p>
 * 用法：
 * <pre>{@code
 * // 请求线程：校验参数后提交，立即返回 logId 给前端打开实时日志
 * String logId = sysTaskExecutor.submit("image-sync", "同步 nginx 到阿里云", () -> {
 *     TaskLog.info("开始...");
 *     // ... 实际工作
 * });
 * return logId;
 * }</pre>
 * <p>
 * 任务在独立线程中执行；{@link TaskWork} 内异常会被捕获并记录为失败，不会向外抛出。
 * 日志文件写入 {@code ${logging.file.path}/{logId}.log}，任务记录写入 {@code sys_task_log}。
 */
@Service
@Slf4j
public class SysTaskExecutor {

    /**
     * logId 白名单：与日志/WS 路径安全校验保持一致。
     */
    private static final Pattern LOG_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    @Resource
    private SysTaskLogService taskLogService;

    @Resource
    private TaskLogRegistry taskLogRegistry;

    private final ThreadPoolTaskExecutor executor;

    public SysTaskExecutor() {
        ThreadPoolTaskExecutor tp = new ThreadPoolTaskExecutor();
        tp.setCorePoolSize(4);
        tp.setMaxPoolSize(16);
        tp.setQueueCapacity(200);
        tp.setThreadNamePrefix("sys-task-");
        tp.setWaitForTasksToCompleteOnShutdown(true);
        tp.setAwaitTerminationSeconds(30);
        tp.initialize();
        this.executor = tp;
    }

    /**
     * 一次长任务的工作体。异常由执行器统一记入日志与任务记录。
     */
    @FunctionalInterface
    public interface TaskWork {
        void run() throws Exception;
    }

    /**
     * 提交任务（最简）：自动生成 logId，日志仅登录用户可查看。
     *
     * @param type  任务类型，如 image-sync / build-test
     * @param title 人类可读描述，任务中心展示
     * @param work  任务体（在异步线程执行，可通过 {@link TaskLog} 输出日志）
     * @return logId，前端可据此打开实时日志
     */
    public String submit(String type, String title, TaskWork work) {
        return submit(null, type, title, null, work);
    }

    /**
     * 提交任务（最全）：可指定 logId 与查看权限。
     *
     * @param logId      日志/任务标识；为 null 或空白时自动生成（{@code type-随机16位}）
     * @param type       任务类型
     * @param title      人类可读描述
     * @param permission 查看日志所需权限码；为 null 或空白时表示仅登录用户可查看
     * @param work       任务体（在异步线程执行）
     * @return 实际使用的 logId
     */
    public String submit(String logId, String type, String title, String permission, TaskWork work) {
        Assert.hasText(type, "任务类型不能为空");
        Assert.hasText(title, "任务标题不能为空");
        Assert.notNull(work, "任务体不能为空");

        String actualLogId = normalizeLogId(logId, type);
        String actualPermission = (permission == null || permission.isBlank()) ? null : permission.trim();
        String triggerUser = taskLogService.currentTriggerUser();

        SysTaskLog existing = taskLogService.findByLogId(actualLogId);
        if (existing != null) {
            if (existing.isRunning()) {
                throw new IllegalStateException("任务正在运行中: " + actualLogId);
            }
            // 已结束记录不允许复用同一 logId，避免覆盖历史与日志歧义
            throw new IllegalStateException("任务记录已存在: " + actualLogId);
        }

        taskLogService.start(actualLogId, type, title, actualPermission, triggerUser);
        taskLogRegistry.start(actualLogId);
        executor.execute(() -> execute(actualLogId, work));
        return actualLogId;
    }

    private String normalizeLogId(String logId, String type) {
        if (StrUtil.isBlank(logId)) {
            return type + "-" + RandomUtil.randomString(16);
        }
        String id = logId.trim();
        Assert.isTrue(LOG_ID_PATTERN.matcher(id).matches(), "logId 不合法（仅允许字母、数字、-、_，长度 1-64）: " + logId);
        return id;
    }

    /**
     * 执行并收尾。内部任务异常只记入任务记录与日志文件，不向外抛出。
     */
    void execute(String logId, TaskWork work) {
        taskLogRegistry.start(logId);
        TaskLog.bind(taskLogKey(logId));
        boolean success = false;
        String error = null;
        try {
            work.run();
            success = true;
        } catch (Exception e) {
            error = e.getMessage();
            TaskLog.error("任务失败: {}", error, e);
        } catch (Throwable t) {
            error = t.getMessage();
            try {
                TaskLog.error("任务失败: {}", error, t);
            } catch (Throwable ignore) {
                log.error("任务失败且写日志异常: logId={}", logId, t);
            }
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

    /**
     * 任务日志文件 key：与 logviewer 权限前缀保持一致（task/{logId} -> task/{logId}.log）
     */
    public static String taskLogKey(String logId) {
        return "task/" + logId;
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
