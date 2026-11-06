package io.github.jiangood.openadmin.modules.job.quartz;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDefinition;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.modules.job.JobRegistrar;
import io.github.jiangood.openadmin.modules.job.entity.JobLogAggregation;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 定时任务声明同步：合并扫描结果与 {@link JobRegistrar} SPI，并补建缺失的 {@code sys_job} 记录。
 *
 * <p>策略：只补建、不覆盖。已有记录的 {@code name} / {@code cron} / {@code enabled} 不会被修改；
 * 旧数据按 {@code jobClass} 回填 {@code jobKey}。{@code jobKey} 唯一且可空，为空时回退按
 * {@code jobClass} 匹配，保证升级平滑。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobRegistrationManager {

    private final SysJobRepository sysJobRepository;

    private final JobDefinitionScanner jobDefinitionScanner;

    private final List<JobRegistrar> jobRegistrars;

    /**
     * 启动时同步：扫描 → 回填 key → 补建缺失记录 → 提示执行类不存在。
     */
    public SyncResult sync() {
        List<JobDefinition> definitions = resolveDefinitions();
        backfillKeys(definitions);
        int registered = registerDefinitions(definitions, null);
        warnMissingClasses();

        long registerable = definitions.stream().filter(JobDefinition::isAutoRegister).count();
        return new SyncResult((int) registerable, registered);
    }

    /**
     * 代码中存在、但数据库中尚未注册的任务，供管理页展示和一键导入。
     */
    public List<JobDefinition> findUnregistered() {
        return resolveDefinitions().stream()
                .filter(JobDefinition::isAutoRegister)
                .filter(def -> !exists(def))
                .toList();
    }

    /**
     * 导入指定 key 的任务；{@code keys} 为空时导入全部未注册任务。
     *
     * @return 实际补建的数量
     */
    public int importJobs(List<String> keys) {
        List<JobDefinition> definitions = resolveDefinitions();
        Set<String> selected = keys == null || keys.isEmpty() ? null : new HashSet<>(keys);
        return registerDefinitions(definitions, selected);
    }

    /**
     * 合并扫描结果与 SPI 声明。SPI 优先；同一任务（同执行类）由后声明的 SPI 覆盖。
     */
    List<JobDefinition> resolveDefinitions() {
        Map<Class<?>, JobDefinition> byClass = new LinkedHashMap<>();
        for (Class<?> cls : jobDefinitionScanner.scan()) {
            JobDefinition definition = fromAnnotation(cls);
            if (definition != null) {
                byClass.put(definition.getJobClass(), definition);
            }
        }

        for (JobRegistrar registrar : jobRegistrars) {
            List<JobDefinition> declared;
            try {
                declared = registrar.getJobs();
            } catch (Exception e) {
                log.warn("读取定时任务 SPI 声明失败，class={}：{}", registrar.getClass().getName(), e.getMessage(), e);
                continue;
            }
            if (declared == null) {
                continue;
            }
            for (JobDefinition item : declared) {
                if (item == null || item.getJobClass() == null) {
                    log.warn("定时任务 SPI 声明缺少 jobClass，已忽略：registrar={}", registrar.getClass().getName());
                    continue;
                }
                Class<? extends Job> cls = item.getJobClass();
                if (!isValidJobClass(cls)) {
                    continue;
                }
                JobDefinition base = byClass.get(cls);
                if (base == null) {
                    base = fromAnnotation(cls);
                }
                if (base == null) {
                    base = defaultDefinition(cls);
                }
                byClass.put(cls, merge(base, item));
            }
        }

        List<JobDefinition> sorted = new ArrayList<>(byClass.values());
        sorted.sort(Comparator.comparing(def -> StrUtil.blankToDefault(def.getJobClassName(), "")));

        // key 重复时保留类名排序靠前的一个，避免同一任务补建出两条记录
        Map<String, JobDefinition> byKey = new LinkedHashMap<>();
        for (JobDefinition definition : sorted) {
            String key = StrUtil.trimToNull(definition.getKey());
            if (key == null) {
                key = definition.getJobClassName();
            }
            if (key.length() > 255) {
                log.warn("定时任务 key 超过 255 个字符，跳过自动注册：key={}, class={}", key, definition.getJobClassName());
                continue;
            }
            definition.setKey(key);

            JobDefinition existing = byKey.putIfAbsent(key, definition);
            if (existing != null) {
                log.error("定时任务 key 重复，已忽略后一个：key={}, 保留 class={}, 忽略 class={}",
                        key, existing.getJobClassName(), definition.getJobClassName());
            }
        }
        return new ArrayList<>(byKey.values());
    }

    private JobDefinition fromAnnotation(Class<?> rawClass) {
        if (!Job.class.isAssignableFrom(rawClass)) {
            return null;
        }
        if (!rawClass.getName().endsWith(SysJob.JOB_SUFFIX)) {
            log.warn("定时任务类名必须以 {} 结尾，跳过自动注册：{}", SysJob.JOB_SUFFIX, rawClass.getName());
            return null;
        }

        JobDescription description = rawClass.getAnnotation(JobDescription.class);
        if (description == null && !BaseJob.class.isAssignableFrom(rawClass)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Class<? extends Job> cls = (Class<? extends Job>) rawClass;
        return JobDefinition.builder()
                .key(resolveKey(cls, description))
                .jobClass(cls)
                .name(resolveLabel(description, cls))
                .cron(description == null ? null : StrUtil.trimToNull(description.defaultCron()))
                .enabled(description != null && description.defaultEnabled())
                .logAggregation(description == null ? JobLogAggregation.PER_RUN : description.logAggregation())
                .autoRegister(description == null || description.autoRegister())
                .build();
    }

    private static JobDefinition defaultDefinition(Class<? extends Job> cls) {
        return JobDefinition.builder()
                .key(cls.getName())
                .jobClass(cls)
                .name(cls.getSimpleName())
                .enabled(false)
                .logAggregation(JobLogAggregation.PER_RUN)
                .autoRegister(true)
                .build();
    }

    /**
     * SPI 声明按字段覆盖注解默认值；未设置的字段（null / 空白）保留注解值。
     * SPI 显式声明的任务始终参与注册，可覆盖 {@code autoRegister=false}。
     */
    private static JobDefinition merge(JobDefinition base, JobDefinition override) {
        return JobDefinition.builder()
                .key(StrUtil.blankToDefault(override.getKey(), base.getKey()))
                .jobClass(base.getJobClass())
                .name(StrUtil.blankToDefault(override.getName(), base.getName()))
                .cron(override.getCron() != null ? override.getCron() : base.getCron())
                .enabled(override.getEnabled() != null ? override.getEnabled() : base.getEnabled())
                .logAggregation(override.getLogAggregation() != null ? override.getLogAggregation() : base.getLogAggregation())
                .jobData(override.getJobData() != null ? override.getJobData() : base.getJobData())
                .extraInfo(StrUtil.isNotBlank(override.getExtraInfo()) ? override.getExtraInfo() : base.getExtraInfo())
                .autoRegister(true)
                .build();
    }

    private static String resolveKey(Class<?> cls, JobDescription description) {
        String key = description == null ? null : StrUtil.trimToNull(description.key());
        return key == null ? cls.getName() : key;
    }

    private static String resolveLabel(JobDescription description, Class<?> cls) {
        if (description == null) {
            return cls.getSimpleName();
        }
        return StrUtil.blankToDefault(description.label(), cls.getSimpleName());
    }

    private static boolean isValidJobClass(Class<?> cls) {
        if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) {
            log.warn("定时任务 SPI 声明了抽象类或接口，已忽略：{}", cls.getName());
            return false;
        }
        if (!cls.getName().endsWith(SysJob.JOB_SUFFIX)) {
            log.warn("定时任务类名必须以 {} 结尾，跳过 SPI 注册：{}", SysJob.JOB_SUFFIX, cls.getName());
            return false;
        }
        return true;
    }

    private int registerDefinitions(List<JobDefinition> definitions, Set<String> selectedKeys) {
        int registered = 0;
        List<SysJob> existingJobs = sysJobRepository.findAll();
        for (JobDefinition definition : definitions) {
            if (!definition.isAutoRegister()) {
                continue;
            }
            if (selectedKeys != null && !selectedKeys.contains(definition.getKey())) {
                continue;
            }
            try {
                if (registerOne(definition, existingJobs)) {
                    registered++;
                }
            } catch (Exception e) {
                log.warn("自动注册定时任务失败，key={}, class={}：{}",
                        definition.getKey(), definition.getJobClassName(), e.getMessage(), e);
            }
        }
        return registered;
    }

    private boolean registerOne(JobDefinition definition, List<SysJob> existingJobs) {
        String jobClass = definition.getJobClassName();
        String key = definition.getKey();

        SysJob byKey = sysJobRepository.findByJobKey(key);
        if (byKey != null) {
            if (!jobClass.equals(byKey.getJobClass())) {
                log.warn("定时任务 key 已存在但执行类不同，保留原记录：key={}, 数据库执行类={}, 代码执行类={}；如需迁移请在「系统管理 → 定时任务」中修改执行类",
                        key, byKey.getJobClass(), jobClass);
            }
            return false;
        }

        // 旧数据没有 jobKey，先按 jobClass 匹配并回填，避免升级后重复补建
        List<SysJob> byClass = sysJobRepository.findByJobClass(jobClass);
        if (!byClass.isEmpty()) {
            if (byClass.size() > 1) {
                log.warn("定时任务存在多条相同执行类的记录，请人工清理：class={}, count={}", jobClass, byClass.size());
            }
            SysJob legacy = byClass.get(0);
            if (StrUtil.isBlank(legacy.getJobKey())) {
                legacy.setJobKey(key);
                sysJobRepository.save(legacy);
                log.info("为已有定时任务回填 jobKey：{} [{}] -> {}", legacy.getName(), jobClass, key);
            } else if (!key.equals(legacy.getJobKey())) {
                log.warn("同一执行类声明了不同的 key，保留数据库中的 key：class={}, 数据库 key={}, 代码 key={}",
                        jobClass, legacy.getJobKey(), key);
            }
            return false;
        }

        // 类改名/换包后 key 与 jobClass 都匹配不到：若旧执行类已不存在且简单类名唯一，
        // 视为疑似换包，跳过补建并提示人工修正，避免产生重复任务
        SysJob moved = findMovedClassRecord(definition, existingJobs);
        if (moved != null) {
            log.warn("定时任务疑似换包，已跳过补建以避免重复：key={}, 代码执行类={}, 数据库记录 id={} 的执行类={} 已不存在；"
                            + "如需继续使用，请在「系统管理 → 定时任务」中把执行类改为新类",
                    key, jobClass, moved.getId(), moved.getJobClass());
            return false;
        }

        String cron = StrUtil.trimToNull(definition.getCron());
        boolean enabled = Boolean.TRUE.equals(definition.getEnabled()) && cron != null;
        if (Boolean.TRUE.equals(definition.getEnabled()) && cron == null) {
            log.warn("定时任务声明了 enabled=true 但 cron 为空，按禁用注册：{}", jobClass);
        }

        String name = resolveName(definition.getName(), definition.getJobClass());
        if (name == null) {
            return false;
        }

        SysJob job = new SysJob();
        job.setJobKey(key);
        job.setName(name);
        job.setCron(cron);
        job.setEnabled(enabled);
        job.setJobClass(jobClass);
        job.setJobData(definition.getJobData());
        job.setExtraInfo(definition.getExtraInfo());
        job.setLogAggregation(definition.getLogAggregation() == null
                ? JobLogAggregation.PER_RUN : definition.getLogAggregation());

        try {
            sysJobRepository.save(job);
        } catch (DataIntegrityViolationException e) {
            log.warn("定时任务已存在（可能由其他实例注册），跳过：key={}, name={}, class={}", key, name, jobClass);
            return false;
        }

        log.info("自动注册定时任务：{} [{}]，key={}, enabled={}", name, jobClass, key, enabled);
        return true;
    }

    /**
     * 存量记录按执行类回填 jobKey；已存在 key 或会与其它记录冲突时跳过。
     */
    private void backfillKeys(List<JobDefinition> definitions) {
        Map<String, String> keyByClass = new LinkedHashMap<>();
        for (JobDefinition definition : definitions) {
            keyByClass.putIfAbsent(definition.getJobClassName(), definition.getKey());
        }

        for (SysJob job : sysJobRepository.findAll()) {
            if (StrUtil.isNotBlank(job.getJobKey()) || StrUtil.isBlank(job.getJobClass())) {
                continue;
            }
            String key = keyByClass.get(job.getJobClass());
            if (key == null) {
                continue;
            }

            SysJob conflict = sysJobRepository.findByJobKey(key);
            if (conflict != null && !conflict.getId().equals(job.getId())) {
                log.warn("回填 jobKey 失败，key 已被其他任务占用：key={}, id={}, 占用 id={}",
                        key, job.getId(), conflict.getId());
                continue;
            }

            job.setJobKey(key);
            sysJobRepository.save(job);
            log.info("为已有定时任务回填 jobKey：{} [{}] -> {}", job.getName(), job.getJobClass(), key);
        }
    }

    /**
     * 查找疑似「类换包」的旧记录：执行类已不存在、简单类名相同，且仍是默认 key（空或等于旧类名）。
     * 仅当唯一匹配时返回，避免误判。
     */
    private SysJob findMovedClassRecord(JobDefinition definition, List<SysJob> existingJobs) {
        String simpleName = definition.getJobClass().getSimpleName();
        String jobClassName = definition.getJobClassName();
        List<SysJob> matches = new ArrayList<>();

        for (SysJob job : existingJobs) {
            if (StrUtil.isBlank(job.getJobClass()) || job.getJobClass().equals(jobClassName)) {
                continue;
            }
            if (classExists(job.getJobClass())) {
                continue;
            }
            if (!simpleName.equals(simpleNameOf(job.getJobClass()))) {
                continue;
            }
            // 显式改过 key 的记录不参与推断，尊重用户对不同身份的声明
            if (StrUtil.isNotBlank(job.getJobKey()) && !job.getJobKey().equals(job.getJobClass())) {
                continue;
            }
            matches.add(job);
        }

        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.size() > 1) {
            log.warn("定时任务疑似换包但存在多条同名记录，已跳过兜底匹配：class={}, simpleName={}, count={}",
                    jobClassName, simpleName, matches.size());
        }
        return null;
    }

    private static String simpleNameOf(String className) {
        int dot = className.lastIndexOf('.');
        String name = dot >= 0 ? className.substring(dot + 1) : className;
        int dollar = name.lastIndexOf('$');
        return dollar >= 0 ? name.substring(dollar + 1) : name;
    }

    /**
     * 名称优先使用 label；若与已有任务重名，追加简单类名，仍冲突则跳过。
     * {@code sys_job.name} 有唯一约束，同时也是 Quartz 的 JobKey，不能重复。
     */
    private String resolveName(String label, Class<?> cls) {
        String base = StrUtil.blankToDefault(StrUtil.trimToNull(label), cls.getSimpleName());
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
                loader = JobRegistrationManager.class.getClassLoader();
            }
            Class.forName(className, false, loader);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    private boolean exists(JobDefinition definition) {
        return sysJobRepository.findByJobKey(definition.getKey()) != null
                || !sysJobRepository.findByJobClass(definition.getJobClassName()).isEmpty();
    }

    public record SyncResult(int scanned, int registered) {
    }

}
