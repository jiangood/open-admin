package io.github.jiangood.openadmin.modules.job.quartz;

import io.github.jiangood.openadmin.modules.job.service.JobLogCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

/**
 * 全局 Quartz JobListener：任意任务执行结束后触发一次过期日志清理的惰性检查
 * <p>
 * 通过 {@link SchedulerFactoryBeanCustomizer} 注册为全局监听器（比挂在 {@code BaseJob}
 * 内覆盖面更广，直接实现 {@code org.quartz.Job} 的任务执行结束同样触发）。
 * 清理本身在 {@link JobLogCleanupService} 内部做了节流与异步隔离，这里只负责触发，
 * 且吞掉一切异常，绝不影响任务执行链路。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobLogCleanupListener implements JobListener, SchedulerFactoryBeanCustomizer {

    private final JobLogCleanupService jobLogCleanupService;

    @Override
    public String getName() {
        return "jobLogCleanup";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        // no-op
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // no-op
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        try {
            jobLogCleanupService.considerCleanup();
        } catch (Exception e) {
            log.warn("触发任务日志清理失败: {}", e.getMessage());
        }
    }

    @Override
    public void customize(SchedulerFactoryBean schedulerFactoryBean) {
        schedulerFactoryBean.setGlobalJobListeners(this);
    }
}
