package io.github.jiangood.openadmin.modules.job.quartz;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Order(1)
public class QuartzInitializer implements CommandLineRunner {

    @Resource
    private SysJobRepository sysJobRepository;

    @Resource
    private QuartzManager quartzService;

    @Resource
    private SystemProperties systemProperties;

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        if (!systemProperties.isJobEnable()) {
            log.warn("定时任务模块已设置全局关闭");
            return;
        }

        List<SysJob> list = sysJobRepository.findAllByEnabledTrue();
        for (SysJob sysJob : list) {
            try {
                log.info("加载定时任务: {} [{}]", sysJob.getName(), sysJob.getJobClass());
                quartzService.scheduleJob(sysJob);
            } catch (ClassNotFoundException e) {
                log.error("加载定时任务失败, id={}, name={}, class={}: {}",
                        sysJob.getId(), sysJob.getName(), sysJob.getJobClass(), e.getMessage());
            } catch (Exception e) {
                log.warn("加载定时任务异常, id={}, name={}: {}",
                        sysJob.getId(), sysJob.getName(), e.getMessage());
            }
        }
    }

}
