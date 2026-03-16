package io.github.jiangood.openadmin.modules.job.quartz;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.List;

/***
 * 作业调度
 *
 */
@Slf4j
@Component
@Configuration
@EnableScheduling
public class QuartzInit implements CommandLineRunner {


    @Resource
    private SysJobRepository sysJobRepository;


    @Resource
    private QuartzManager quartzService;

    @Resource
    private SystemProperties systemProperties;


    @Override
    public void run(String... args) throws Exception {
        if (!systemProperties.isJobEnable()) {
            log.warn("定时任务模块已设置全局关闭");
            return;
        }

        // 2. 加载数据库任务
        List<SysJob> list = sysJobRepository.findAllByEnabledTrue();
        for (SysJob sysJob : list) {
            try {
                log.info("加载定时任务: {} {}", sysJob.getName(), sysJob.getJobClass());
                quartzService.scheduleJob(sysJob);
            } catch (ClassNotFoundException e) {
                log.error("加载数据库任务失败：{}", e.getMessage());
            }
        }
    }


}
