package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.logviewer.service.FileLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 定时清理过期的任务执行记录及对应日志文件
 * <p>
 * 保留天数由 sys.job-log-retain-days 配置，&lt;=0 表示不清理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobLogCleanupTask {

    private static final int BATCH_SIZE = 200;

    private final SystemProperties systemProperties;
    private final SysJobLogRepository sysJobLogRepository;
    private final FileLogService fileLogService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredLogs() {
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
