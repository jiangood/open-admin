package io.github.jiangood.openadmin.modules.job.service;

import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.modules.job.dto.JobStat;
import io.github.jiangood.openadmin.modules.job.dto.LogSizeStat;
import io.github.jiangood.openadmin.modules.job.progress.JobProgress;
import io.github.jiangood.openadmin.modules.job.progress.JobProgressRegistry;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 定时任务页面顶部卡片所需的统计信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobStatService {

    private final SysJobRepository sysJobRepository;
    private final SysJobLogRepository sysJobLogRepository;
    private final JobProgressRegistry jobProgressRegistry;
    private final JobLogSizeService jobLogSizeService;
    private final Scheduler scheduler;

    /**
     * 任务规模、运行中数量、今日执行情况与调度器信息
     */
    public JobStat stat() {
        JobStat stat = new JobStat();
        stat.setJobCount(sysJobRepository.count());
        stat.setEnabledCount(sysJobRepository.countByEnabled(true));
        stat.setRunningCount(jobProgressRegistry.listAll().stream()
                .filter(JobProgress::isRunning)
                .count());

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stat.setTodayExecuteCount(nullToZero(sysJobLogRepository.sumExecuteCountSince(todayStart)));
        stat.setTodayFailCount(nullToZero(sysJobLogRepository.sumFailCountSince(todayStart)));

        stat.setScheduler(schedulerInfo());
        return stat;
    }

    /**
     * 全部任务日志文件占用
     */
    public LogSizeStat totalLogSize() {
        return jobLogSizeService.total();
    }

    /**
     * 单个任务的日志文件占用
     */
    public LogSizeStat logSizeOfJob(String jobId) {
        return jobLogSizeService.forJob(jobId);
    }

    private JobStat.Scheduler schedulerInfo() {
        JobStat.Scheduler info = new JobStat.Scheduler();
        try {
            SchedulerMetaData meta = scheduler.getMetaData();
            info.setVersion(meta.getVersion());
            info.setSchedulerName(meta.getSchedulerName());
            info.setInstanceId(meta.getSchedulerInstanceId());
            info.setStarted(!meta.isShutdown() && meta.getRunningSince() != null);
            info.setStandby(meta.isInStandbyMode());
            info.setRunningSince(meta.getRunningSince() == null
                    ? null : DateUtil.formatDateTime(meta.getRunningSince()));
            info.setJobsExecuted(meta.getNumberOfJobsExecuted());
            info.setThreadPoolSize(meta.getThreadPoolSize());
            info.setJobStoreClass(meta.getJobStoreClass() == null
                    ? null : meta.getJobStoreClass().getSimpleName());
            info.setPersistent(meta.isJobStoreSupportsPersistence());
            info.setClustered(meta.isJobStoreClustered());
        } catch (SchedulerException e) {
            log.warn("读取 Quartz 调度器状态失败: {}", e.getMessage());
            info.setError(e.getMessage());
        }
        return info;
    }

    private static long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
