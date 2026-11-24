package io.github.jiangood.openadmin.modules.job.service;

import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.modules.job.JobStatus;
import io.github.jiangood.openadmin.modules.job.dto.JobExecStat;
import io.github.jiangood.openadmin.modules.job.dto.JobStat;
import io.github.jiangood.openadmin.modules.job.dto.LogSizeStat;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务页面顶部卡片所需的统计信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobStatService {

    private final SysJobRepository sysJobRepository;
    private final SysJobLogRepository sysJobLogRepository;
    private final JobLogSizeService jobLogSizeService;
    private final Scheduler scheduler;

    /**
     * 任务规模、今日执行情况与调度器信息
     */
    public JobStat stat() {
        JobStat stat = new JobStat();
        stat.setJobCount(sysJobRepository.count());
        stat.setEnabledCount(sysJobRepository.countByEnabled(true));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Map<JobStatus, Long> counts = toStatusCounts(sysJobLogRepository.countGroupByStatusSince(todayStart));
        stat.setTodayExecuteCount(sum(counts));
        stat.setTodayFailCount(sumFailure(counts));

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

    /**
     * 单个任务的执行统计
     */
    public JobExecStat jobExecStat(String jobId) {
        JobExecStat stat = new JobExecStat();
        Map<JobStatus, Long> counts = toStatusCounts(sysJobLogRepository.countGroupByStatusByJobId(jobId));

        stat.setRecordCount(sum(counts));
        stat.setSuccessCount(counts.getOrDefault(JobStatus.SUCCESS, 0L));
        stat.setPartialCount(counts.getOrDefault(JobStatus.PARTIAL, 0L));
        stat.setSkipCount(counts.getOrDefault(JobStatus.SKIPPED, 0L));
        stat.setFailCount(counts.getOrDefault(JobStatus.FAILED, 0L));
        stat.setExceptionCount(counts.getOrDefault(JobStatus.EXCEPTION, 0L));
        stat.setCancelledCount(counts.getOrDefault(JobStatus.CANCELLED, 0L));
        stat.setRunningCount(counts.getOrDefault(JobStatus.RUNNING, 0L));

        sysJobLogRepository.findFirstBySysJob_IdOrderByBeginTimeDesc(jobId).ifPresent(log -> {
            stat.setLastBeginTime(formatTime(log.getBeginTime()));
            stat.setLastEndTime(formatTime(log.getEndTime()));
            stat.setLastStatus(log.getStatus());
            stat.setLastResult(log.getResult());
            stat.setLastRunTime(log.getJobRunTime());
        });
        return stat;
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

    /**
     * 把 {@code [JobStatus, Long]} 行转换为按状态的计数
     */
    static Map<JobStatus, Long> toStatusCounts(List<Object[]> rows) {
        Map<JobStatus, Long> counts = new EnumMap<>(JobStatus.class);
        if (rows == null) {
            return counts;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null) {
                continue;
            }
            counts.put((JobStatus) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    static long sum(Map<JobStatus, Long> counts) {
        return counts.values().stream().mapToLong(Long::longValue).sum();
    }

    static long sumFailure(Map<JobStatus, Long> counts) {
        return JobStatus.FAILURE_STATUSES.stream().mapToLong(s -> counts.getOrDefault(s, 0L)).sum();
    }

    private static String formatTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
