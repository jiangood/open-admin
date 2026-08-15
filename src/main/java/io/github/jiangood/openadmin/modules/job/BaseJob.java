package io.github.jiangood.openadmin.modules.job;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import io.github.jiangood.openadmin.modules.logviewer.util.FileLogTool;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@DisallowConcurrentExecution
public abstract class BaseJob implements Job {

    @Resource
    private SysJobLogRepository sysJobLogRepository;

    @Resource
    private SysJobRepository sysJobRepository;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();


        String jobName = context.getJobDetail().getKey().getName();

        SysJob job = sysJobRepository.findByName(jobName);

        SysJobLog jobLog = new SysJobLog();
        jobLog.setSysJob(job);
        Date fireTime = context.getFireTime(); // NOSONAR: Quartz JobExecutionContext.getFireTime() 返回 java.util.Date，外部 API 不可改
        jobLog.setBeginTime(fireTime == null ? null : LocalDateTime.ofInstant(fireTime.toInstant(), ZoneId.systemDefault()));
        jobLog = sysJobLogRepository.save(jobLog);


        Logger logger = FileLogTool.getLogger("job/" + jobLog.getId());
        logger.info("开始执行操作");

        String result;
        try {
            result = this.execute(data, logger);
        } catch (Exception e) {
            logger.error("任务执行异常", e);
            result = "异常" + e.getMessage();
            jobLog.setSuccess(false);
        }

        jobLog.setJobRunTime(fireTime == null ? null : System.currentTimeMillis() - fireTime.getTime());
        jobLog.setResult(result);
        jobLog.setEndTime(LocalDateTime.now(ZoneId.systemDefault()));
        sysJobLogRepository.save(jobLog);
        logger.info("执行结束 返回值{}", result);
        FileLogTool.clear();
    }

    public abstract String execute(JobDataMap data, Logger logger) throws Exception; // NOSONAR: 任务实现体允许抛出任意受检异常，框架统一捕获记录
}
