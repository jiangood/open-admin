package io.github.jiangood.openadmin.modules.job;

import io.github.jiangood.openadmin.modules.job.dao.SysJobDao;
import io.github.jiangood.openadmin.modules.job.dao.SysJobExecuteRecordDao;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobExecuteRecord;
import io.github.jiangood.openadmin.modules.log.file.FileLogTool;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.slf4j.Logger;

import java.util.Date;

@DisallowConcurrentExecution // 不允许并发
public abstract class BaseJob implements Job {

    @Resource
    private SysJobExecuteRecordDao sysJobLogDao;

    @Resource
    private SysJobDao sysJobDao;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();


        String jobName = context.getJobDetail().getKey().getName();

        // 1. 数据库保存记录
        SysJob job = sysJobDao.findByName(jobName);

        SysJobExecuteRecord jobLog = new SysJobExecuteRecord();
        jobLog.setSysJob(job);
        Date fireTime = context.getFireTime();
        jobLog.setBeginTime(fireTime);
        jobLog = sysJobLogDao.save(jobLog);


        // 2. 设置日志
        Logger logger = FileLogTool.getLogger(jobLog.getId());
        logger.info("开始执行作物");

        String result;
        try {
            result = this.execute(data, logger);
        } catch (Exception e) {
            logger.error("任务执行异常", e);
            result = "异常" + e.getMessage();
            jobLog.setSuccess(false);
        }

        jobLog.setJobRunTime(System.currentTimeMillis() - fireTime.getTime());
        jobLog.setResult(result);
        jobLog.setEndTime(new Date());
        sysJobLogDao.save(jobLog);
        logger.info("执行结束 返回值{}", result);
        FileLogTool.clear();
    }

    public abstract String execute(JobDataMap data, Logger logger) throws Exception;
}
