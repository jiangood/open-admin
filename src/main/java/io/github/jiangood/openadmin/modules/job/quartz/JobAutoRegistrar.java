package io.github.jiangood.openadmin.modules.job.quartz;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动时把代码中声明的定时任务补建到 {@code sys_job}。
 *
 * <p>扫描范围：{@link BaseJob} 子类，以及显式带 {@link JobDescription} 的 {@code Job} 实现，
 * 同时合并业务 {@code JobRegistrar} SPI 声明。策略：只补建、不覆盖；补建的任务默认禁用，
 * 用户可在「系统管理 → 定时任务」中修改 cron、名称与启用状态。数据库中存在但执行类已不存在的
 * 记录只记录 warn，不自动删除。</p>
 *
 * <p>执行顺序放在种子数据之后、{@link QuartzInitializer} 之前，避免与框架内置任务的 seed
 * 重复，也保证补建后由 {@code QuartzInitializer} 统一调度。</p>
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class JobAutoRegistrar implements CommandLineRunner {

    private final SystemProperties systemProperties;

    private final JobRegistrationManager registrationManager;

    @Override
    public void run(String... args) {
        if (!systemProperties.isJobEnable()) {
            log.warn("定时任务模块已设置全局关闭，跳过自动注册");
            return;
        }
        if (!systemProperties.isJobAutoRegister()) {
            log.info("定时任务自动注册已关闭");
            return;
        }

        JobRegistrationManager.SyncResult result = registrationManager.sync();
        log.info("定时任务自动注册完成，扫描 {} 个，补建 {} 个", result.scanned(), result.registered());
    }
}
