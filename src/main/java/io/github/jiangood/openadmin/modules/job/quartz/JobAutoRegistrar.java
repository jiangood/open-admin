package io.github.jiangood.openadmin.modules.job.quartz;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.modules.job.entity.JobLogAggregation;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时把代码中声明的定时任务补建到 {@code sys_job}。
 *
 * <p>扫描范围：{@link BaseJob} 子类，以及显式带 {@link JobDescription} 的 {@code Job} 实现。
 * 策略：只补建、不覆盖；补建的任务默认禁用，用户可在「系统管理 → 定时任务」中修改
 * cron、名称与启用状态。数据库中存在但执行类已不存在的记录只记录 warn，不自动删除。</p>
 *
 * <p>执行顺序放在种子数据之后、{@link QuartzInitializer} 之前，避免与框架内置任务的 seed
 * 重复，也保证补建后由 {@code QuartzInitializer} 统一调度。</p>
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class JobAutoRegistrar implements CommandLineRunner {

    private final SysJobRepository sysJobRepository;

    private final SystemProperties systemProperties;

    private final JobDefinitionScanner jobDefinitionScanner;

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

        List<Class<?>> classes = jobDefinitionScanner.scan();
        int registered = 0;
        for (Class<?> cls : classes) {
            try {
                if (registerOne(cls)) {
                    registered++;
                }
            } catch (Exception e) {
                log.warn("自动注册定时任务失败，class={}：{}", cls.getName(), e.getMessage(), e);
            }
        }

        warnMissingClasses();
        log.info("定时任务自动注册完成，扫描 {} 个，补建 {} 个", classes.size(), registered);
    }

    private boolean registerOne(Class<?> cls) {
        if (!Job.class.isAssignableFrom(cls)) {
            return false;
        }
        JobDescription description = cls.getAnnotation(JobDescription.class);
        if (description != null && !description.autoRegister()) {
            return false;
        }
        if (description == null && !BaseJob.class.isAssignableFrom(cls)) {
            // 只注册 BaseJob 子类或显式声明 @JobDescription 的 Job，避免把历史 Job 实现全部补进来
            return false;
        }

        String jobClass = cls.getName();
        if (!jobClass.endsWith(SysJob.JOB_SUFFIX)) {
            log.warn("定时任务类名必须以 {} 结尾，跳过自动注册：{}", SysJob.JOB_SUFFIX, jobClass);
            return false;
        }

        List<SysJob> existing = sysJobRepository.findByJobClass(jobClass);
        if (!existing.isEmpty()) {
            if (existing.size() > 1) {
                log.warn("定时任务存在多条相同执行类的记录，请人工清理：class={}, count={}", jobClass, existing.size());
            }
            return false;
        }

        String name = resolveName(description == null ? null : description.label(), cls);
        if (name == null) {
            return false;
        }

        String cron = description == null ? null : StrUtil.trimToNull(description.defaultCron());
        boolean enabled = description != null && description.defaultEnabled() && cron != null;
        if (description != null && description.defaultEnabled() && cron == null) {
            log.warn("定时任务声明了 defaultEnabled=true 但 defaultCron 为空，按禁用注册：{}", jobClass);
        }

        SysJob job = new SysJob();
        job.setName(name);
        job.setCron(cron);
        job.setEnabled(enabled);
        job.setJobClass(jobClass);
        job.setLogAggregation(description == null ? JobLogAggregation.PER_RUN : description.logAggregation());

        try {
            sysJobRepository.save(job);
        } catch (DataIntegrityViolationException e) {
            log.warn("定时任务已存在（可能由其他实例注册），跳过：name={}, class={}", name, jobClass);
            return false;
        }

        log.info("自动注册定时任务：{} [{}]，enabled={}", name, jobClass, enabled);
        return true;
    }

    /**
     * 名称优先使用 label；若与已有任务重名，追加简单类名，仍冲突则跳过。
     * {@code sys_job.name} 有唯一约束，同时也是 Quartz 的 JobKey，不能重复。
     */
    private String resolveName(String label, Class<?> cls) {
        String base = StrUtil.blankToDefault(label, cls.getSimpleName());
        if (sysJobRepository.findByName(base) == null) {
            return base;
        }

        String withClass = base + "（" + cls.getSimpleName() + "）";
        if (sysJobRepository.findByName(withClass) == null) {
            return withClass;
        }

        String simpleName = cls.getSimpleName();
        if (sysJobRepository.findByName(simpleName) == null) {
            return simpleName;
        }

        log.warn("无法为定时任务生成唯一名称，跳过自动注册：label={}, class={}", base, cls.getName());
        return null;
    }

    private void warnMissingClasses() {
        for (SysJob job : sysJobRepository.findAll()) {
            if (StrUtil.isBlank(job.getJobClass())) {
                continue;
            }
            if (!classExists(job.getJobClass())) {
                log.warn("定时任务记录的执行类不存在，请检查或删除：id={}, name={}, class={}",
                        job.getId(), job.getName(), job.getJobClass());
            }
        }
    }

    private static boolean classExists(String className) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = JobAutoRegistrar.class.getClassLoader();
            }
            Class.forName(className, false, loader);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
