package io.github.jiangood.openadmin;

import io.github.jiangood.openadmin.framework.data.impl.BaseRepositoryImpl;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@Configuration
@ComponentScan(basePackages = OpenAdminConfiguration.PKG)
@EntityScan(basePackages = OpenAdminConfiguration.PKG)
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaRepositories(
        repositoryBaseClass = BaseRepositoryImpl.class
)
public class OpenAdminConfiguration {

    public static final String PKG =  "io.github.jiangood.openadmin";

    @Bean("operationLogExecutor")
    public Executor operationLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("oplog-");
        executor.setDaemon(true);
        return executor;
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("sched-");
        scheduler.setDaemon(true);
        return scheduler;
    }
}
