package io.github.jiangood.openadmin.modules.job;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.progress.JobProgressRegistry;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import io.github.jiangood.openadmin.modules.logviewer.util.TaskLog;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@DisallowConcurrentExecution
public abstract class BaseJob implements Job, InterruptableJob {

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

    /**
     * 当前正在执行任务的线程：取消时通过中断该线程触发协作式取消。
     * <p>
     * 任务类为 {@link DisallowConcurrentExecution}，同一任务同一时刻只会有一个执行实例。
     */
    private volatile Thread executingThread;


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

        String previousLogKey = TaskLog.bind(JobLogPermissionProvider.KEY_PREFIX + "/" + jobLog.getId());
        Logger logger = TaskLog.logger();
        if (jobLog.getLogBucket() != null) {
            int nextRun = (jobLog.getExecuteCount() == null ? 0 : jobLog.getExecuteCount()) + 1;
            logger.info("===== 第 {} 次执行 =====", nextRun);
        }
        logger.info("开始执行操作");

        long startMillis = System.currentTimeMillis();
        String result = null;
        boolean success = true;
        executingThread = Thread.currentThread();
        try {
            result = this.execute(data, logger);
            jobProgressRegistry.success(currentJobId, result);
        } catch (InterruptedException e) {
            // 管理端取消：Quartz 中断执行线程后，任务以 InterruptedException 结束
            Thread.currentThread().interrupt();
            logger.warn("任务已被取消执行");
            result = "已取消";
            success = false;
            jobProgressRegistry.fail(currentJobId, result);
        } catch (Exception e) {
            logger.error("任务执行异常", e);
            result = "异常" + e.getMessage();
            success = false;
            jobProgressRegistry.fail(currentJobId, result);
        } finally {
            executingThread = null;
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
                TaskLog.unbind(previousLogKey);
            }
        }
    }

    /**
     * 获取本次执行要写入的执行记录：每次执行新建一条记录，日志文件为 job/{logId}.log。
     */
    private SysJobLog acquireLog(SysJob job, LocalDateTime fireTime) {
        SysJobLog jobLog = new SysJobLog();
        jobLog.setSysJob(job);
        jobLog.setLogBucket(null);
        jobLog.setBeginTime(fireTime);
        jobLog.setExecuteCount(0);
        jobLog.setFailCount(0);
        jobLog.setTotalRunTime(0L);
        return sysJobLogRepository.save(jobLog);
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

    /**
     * Quartz 取消执行：中断当前执行线程。
     * <p>
     * 任务实现若在长循环/阻塞调用中需要及时响应取消，应主动检查
     * {@link Thread#isInterrupted()} 或让阻塞调用抛出 {@link InterruptedException}。
     */
    @Override
    public void interrupt() {
        Thread thread = this.executingThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public abstract String execute(JobDataMap data, Logger logger) throws Exception;
    /**
     * 更新运行状态描述（可选，用于在任务列表/日志中展示当前阶段）。
     *
     * @param message 状态描述
     */
    protected void progress(String message) {
        jobProgressRegistry.update(currentJobId, message);
    }
}