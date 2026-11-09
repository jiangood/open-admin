package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.logviewer.service.FileLogService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定时任务执行记录及日志文件的惰性清理
 * <p>
 * 不再用每天 03:00 的固定调度，而是任意任务执行结束后调用 {@link #considerCleanup()} 触发检查：
 * 同一自然日最多触发一次（节流），实际清理在独立线程异步执行，不阻塞 Quartz 线程。
 * 没有任务执行就不会触发清理，天然把清理开销与业务活跃度对齐。
 * <p>
 * 保留天数由 sys.job-log-retain-days 配置，&lt;=0 表示不清理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobLogCleanupService {

    private static final int BATCH_SIZE = 200;

    private final SystemProperties systemProperties;
    private final SysJobLogRepository sysJobLogRepository;
    private final FileLogService fileLogService;
    private final JobLogSizeService jobLogSizeService;

    /**
     * 最近一次触发清理的自然日，同一天只触发一次；清理幂等，重启后重复执行无副作用
     */
    private volatile String lastCleanupDay;

    /**
     * 是否有清理在执行，防止并发重入
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ExecutorService executor;

    @PostConstruct
    void initExecutor() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "job-log-cleanup");
            thread.setDaemon(true);
            return thread;
        });
    }

    @PreDestroy
    void shutdownExecutor() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * 任务执行结束后调用：一天最多触发一次过期清理检查，实际清理异步执行
     */
    public void considerCleanup() {
        if (systemProperties.getJobLogRetainDays() <= 0) {
            return;
        }
        String today = currentDay();
        if (today.equals(lastCleanupDay)) {
            return;
        }
        if (!running.compareAndSet(false, true)) {
            // 已有清理排队/执行中，不重复触发；不占用当日额度，下次任务执行可重试
            return;
        }
        try {
            runAsync(() -> {
                try {
                    cleanExpiredLogs();
                } catch (Exception e) {
                    log.warn("任务日志清理失败: {}", e.getMessage());
                } finally {
                    running.set(false);
                }
            });
            // 提交成功才占用当日额度；跨天挂起的清理不会吞掉新的一天
            lastCleanupDay = today;
        } catch (Exception e) {
            running.set(false);
            log.warn("提交任务日志清理失败: {}", e.getMessage());
        }
    }

    /**
     * 当前自然日，独立出来便于测试注入
     */
    String currentDay() {
        return LocalDate.now().toString();
    }

    /**
     * 异步提交清理任务，独立出来便于测试替换执行方式
     */
    void runAsync(Runnable task) {
        executor.execute(task);
    }

    private void cleanExpiredLogs() {
        int retainDays = systemProperties.getJobLogRetainDays();
        if (retainDays <= 0) {
            return;
        }

        LocalDateTime deadline = LocalDateTime.now(ZoneId.systemDefault()).minusDays(retainDays);
        int total = 0;
        while (true) {
            List<SysJobLog> batch = sysJobLogRepository
                    .findByUpdateTimeBefore(deadline, PageRequest.of(0, BATCH_SIZE))
                    .getContent();
            if (batch.isEmpty()) {
                break;
            }
            sysJobLogRepository.deleteAll(batch);
            for (SysJobLog jobLog : batch) {
                deleteLogFileQuietly(jobLog.getId());
            }
            total += batch.size();
        }

        if (total > 0) {
            jobLogSizeService.invalidate();
            log.info("任务执行记录清理完成，共删除 {} 条（保留 {} 天）", total, retainDays);
        }
    }

    private void deleteLogFileQuietly(String logId) {
        try {
            fileLogService.deleteLog("job/" + logId);
        } catch (Exception e) {
            log.warn("删除任务日志文件失败: logId={}, {}", logId, e.getMessage());
        }
    }
}
