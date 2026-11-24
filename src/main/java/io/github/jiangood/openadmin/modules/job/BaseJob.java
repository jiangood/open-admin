package io.github.jiangood.openadmin.modules.job;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
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

    /**
     * result 列默认 varchar(255)，统一截断，避免超长消息导致落库失败
     */
    private static final int RESULT_MAX_LENGTH = 240;

    @Resource
    private SysJobLogRepository sysJobLogRepository;

    @Resource
    private SysJobRepository sysJobRepository;

    /**
     * 当前正在执行任务的线程：取消时通过中断该线程触发协作式取消。
     * <p>
     * 任务类为 {@link DisallowConcurrentExecution}，同一任务同一时刻只会有一个执行实例。
     */
    private volatile Thread executingThread;


    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();

        String jobName = context.getJobDetail().getKey().getName();

        SysJob job = sysJobRepository.findByName(jobName);
        if (job == null) {
            // 任务定义已被删除（例如手工删库），无法关联执行记录，直接跳过
            LoggerFactory.getLogger(BaseJob.class).warn("定时任务定义不存在，跳过执行: {}", jobName);
            return;
        }

        long fireTimeMillis = context.getFireTime() == null ? System.currentTimeMillis() : context.getFireTime().getTime();
        LocalDateTime fireTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(fireTimeMillis), ZoneId.systemDefault());

        SysJobLog jobLog = acquireLog(job, fireTime, context.getFireInstanceId());

        String previousLogKey = TaskLog.bind(JobLogPermissionProvider.KEY_PREFIX + "/" + jobLog.getId());
        Logger logger = TaskLog.logger();
        logger.info("开始执行操作");

        long startMillis = System.currentTimeMillis();
        JobResult result = null;
        Error fatal = null;
        executingThread = Thread.currentThread();
        try {
            result = this.execute(data, logger);
        } catch (InterruptedException e) {
            // 管理端取消：Quartz 中断执行线程后，任务以 InterruptedException 结束
            Thread.currentThread().interrupt();
            logger.warn("任务已被取消执行");
            result = new JobResult(JobStatus.CANCELLED, "已取消");
        } catch (Throwable e) {
            logger.error("任务执行异常", e);
            result = new JobResult(JobStatus.EXCEPTION, errorMessage(e));
            if (e instanceof Error error) {
                // Error（如 OOM）记录后继续向上抛出，交给 Quartz / 容器处理
                fatal = error;
            }
        } finally {
            executingThread = null;
            try {
                finishLog(jobLog, normalize(result), System.currentTimeMillis() - startMillis, logger);
            } finally {
                TaskLog.unbind(previousLogKey);
            }
        }

        if (fatal != null) {
            throw fatal;
        }
    }

    /**
     * 获取本次执行要写入的执行记录：每次执行新建一条记录，先落 RUNNING，结束时更新为终态。
     */
    private SysJobLog acquireLog(SysJob job, LocalDateTime fireTime, String fireInstanceId) {
        SysJobLog jobLog = new SysJobLog();
        jobLog.setSysJob(job);
        jobLog.setStatus(JobStatus.RUNNING);
        jobLog.setFireInstanceId(fireInstanceId);
        jobLog.setBeginTime(fireTime);
        return sysJobLogRepository.save(jobLog);
    }

    private void finishLog(SysJobLog jobLog, JobResult result, long duration, Logger logger) {
        try {
            jobLog.setStatus(result.status());
            jobLog.setResult(result.message());
            jobLog.setJobRunTime(duration);
            jobLog.setEndTime(LocalDateTime.now(ZoneId.systemDefault()));
            sysJobLogRepository.save(jobLog);
            logger.info("执行结束 状态{} 返回值{}", result.status().getLabel(), result.message());
        } catch (Exception e) {
            // 执行记录写入失败不应反转任务本身的结论
            logger.error("保存任务执行记录失败", e);
        }
    }

    /**
     * 规范任务返回值：空结果、缺状态、返回 RUNNING 都按异常处理；空文案用状态中文名兜底
     */
    private static JobResult normalize(JobResult result) {
        if (result == null) {
            return new JobResult(JobStatus.EXCEPTION, "任务执行完成但未返回结果");
        }
        if (result.status() == null) {
            return new JobResult(JobStatus.EXCEPTION, "返回结果缺少状态");
        }
        if (result.status() == JobStatus.RUNNING) {
            return new JobResult(JobStatus.EXCEPTION, "任务不能返回运行中状态");
        }
        String message = result.message();
        if (message == null || message.isBlank()) {
            message = result.status().getLabel();
        }
        if (message.length() > RESULT_MAX_LENGTH) {
            message = message.substring(0, RESULT_MAX_LENGTH);
        }
        return new JobResult(result.status(), message);
    }

    private static String errorMessage(Throwable e) {
        String message = e.getMessage();
        return (message == null || message.isBlank()) ? e.getClass().getSimpleName() : message;
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

    public abstract JobResult execute(JobDataMap data, Logger logger) throws Exception;
}
