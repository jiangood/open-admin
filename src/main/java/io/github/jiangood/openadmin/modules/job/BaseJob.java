package io.github.jiangood.openadmin.modules.job;

import io.github.jiangood.openadmin.modules.job.entity.JobLogAggregation;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.progress.JobProgressRegistry;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import io.github.jiangood.openadmin.modules.logviewer.util.FileLog;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@DisallowConcurrentExecution
public abstract class BaseJob implements Job {

    private static final DateTimeFormatter HOURLY_BUCKET = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
    private static final DateTimeFormatter DAILY_BUCKET = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private SysJobLogRepository sysJobLogRepository;

    @Resource
    private SysJobRepository sysJobRepository;

    @Resource
    private JobProgressRegistry jobProgressRegistry;

    /**
     * 本次执行的进度 key（SysJob.id），每次触发新建实例，无需考虑并发
     */
    private String currentJobId;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();


        String jobName = context.getJobDetail().getKey().getName();

        SysJob job = sysJobRepository.findByName(jobName);
        currentJobId = job == null ? null : job.getId();
        if (job == null) {
            // 任务定义已被删除（例如手工删库），无法关联执行记录，直接跳过
            LoggerFactory.getLogger(BaseJob.class).warn("定时任务定义不存在，跳过执行: {}", jobName);
            return;
        }

        long fireTimeMillis = context.getFireTime() == null ? System.currentTimeMillis() : context.getFireTime().getTime();
        LocalDateTime fireTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fireTimeMillis), ZoneId.systemDefault());

        SysJobLog jobLog = acquireLog(job, fireTime);

        jobProgressRegistry.start(job, jobLog.getId());

        FileLog.bind(JobLogPermissionProvider.KEY_PREFIX + "/" + jobLog.getId());
        Logger logger = FileLog.logger();
        if (jobLog.getLogBucket() != null) {
            int nextRun = (jobLog.getExecuteCount() == null ? 0 : jobLog.getExecuteCount()) + 1;
            logger.info("===== 第 {} 次执行 =====", nextRun);
        }
        logger.info("开始执行操作");

        long startMillis = System.currentTimeMillis();
        String result = null;
        boolean success = true;
        try {
            result = this.execute(data, logger);
            jobProgressRegistry.success(currentJobId, result);
        } catch (Exception e) {
            logger.error("任务执行异常", e);
            result = "异常" + e.getMessage();
            success = false;
            jobProgressRegistry.fail(currentJobId, result);
        } finally {
            // 兜底：任务抛出 Error 或提前中断时，避免进度永远停留在「运行中」，并保证执行记录落库
            jobProgressRegistry.failIfRunning(currentJobId, "任务异常终止");
            try {
                if (result == null) {
                    result = "任务异常终止";
                    success = false;
                }
                finishLog(jobLog, success, result, System.currentTimeMillis() - startMillis);
                logger.info("执行结束 返回值{}", result);
            } finally {
                FileLog.unbind();
            }
        }
    }

    /**
     * 获取本次执行要写入的执行记录。
     * <p>
     * 每次执行模式：新建一条记录，日志文件为 job/{logId}.log；
     * 聚合模式：复用同一周期（按小时/按天）的记录，logId 不变，日志追加到同一文件。
     */
    private SysJobLog acquireLog(SysJob job, LocalDateTime fireTime) {
        String bucket = calcBucket(job.getLogAggregation(), fireTime);
        SysJobLog jobLog = null;
        if (bucket != null) {
            jobLog = sysJobLogRepository.findFirstBySysJob_IdAndLogBucket(job.getId(), bucket).orElse(null);
        }
        if (jobLog == null) {
            jobLog = new SysJobLog();
            jobLog.setSysJob(job);
            jobLog.setLogBucket(bucket);
            jobLog.setBeginTime(fireTime);
            jobLog.setExecuteCount(0);
            jobLog.setFailCount(0);
            jobLog.setTotalRunTime(0L);
            jobLog = sysJobLogRepository.save(jobLog);
        }
        return jobLog;
    }

    private void finishLog(SysJobLog jobLog, boolean success, String result, long duration) {
        int count = (jobLog.getExecuteCount() == null ? 0 : jobLog.getExecuteCount()) + 1;
        int failed = (jobLog.getFailCount() == null ? 0 : jobLog.getFailCount()) + (success ? 0 : 1);
        long total = (jobLog.getTotalRunTime() == null ? 0 : jobLog.getTotalRunTime()) + duration;

        jobLog.setExecuteCount(count);
        jobLog.setFailCount(failed);
        jobLog.setTotalRunTime(total);
        jobLog.setSuccess(failed == 0);
        jobLog.setJobRunTime(duration);
        jobLog.setResult(result);
        jobLog.setEndTime(LocalDateTime.now(ZoneId.systemDefault()));
        sysJobLogRepository.save(jobLog);
    }

    private static String calcBucket(JobLogAggregation aggregation, LocalDateTime fireTime) {
        if (aggregation == null) {
            return null;
        }
        return switch (aggregation) {
            case PER_RUN -> null;
            case HOURLY -> fireTime.format(HOURLY_BUCKET);
            case DAILY -> fireTime.format(DAILY_BUCKET);
        };
    }

    public abstract String execute(JobDataMap data, Logger logger) throws Exception;

    /**
     * 上报进度百分比
     *
     * @param percent 0-100
     * @param message 进度描述，可为 null
     */
    protected void progress(int percent, String message) {
        jobProgressRegistry.update(currentJobId, percent, message);
    }

    /**
     * 上报「已处理/总数」进度，百分比自动计算
     *
     * @param current 已处理数量
     * @param total   总数量
     * @param message 进度描述，可为 null
     */
    protected void progress(long current, long total, String message) {
        jobProgressRegistry.update(currentJobId, current, total, message);
    }

    /**
     * 只更新进度描述（进度未知时使用）
     *
     * @param message 进度描述
     */
    protected void progress(String message) {
        jobProgressRegistry.update(currentJobId, null, message);
    }
}
