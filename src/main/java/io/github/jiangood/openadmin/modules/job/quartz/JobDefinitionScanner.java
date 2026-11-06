package io.github.jiangood.openadmin.modules.job.quartz;

import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.util.ClassTool;
import io.github.jiangood.openadmin.util.SpringTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 扫描定时任务类：{@link BaseJob} 子类，或带 {@link JobDescription} 的 {@link Job} 实现。
 *
 * <p>只返回可实例化的具体类；抽象类、接口、局部/匿名类会被忽略。
 * 扫描范围与代码生成一致：框架包 + 业务 {@code @SpringBootApplication} / {@code @ComponentScan} 包。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobDefinitionScanner {

    private final ApplicationContext applicationContext;

    public List<Class<?>> scan() {
        Set<String> packages = new LinkedHashSet<>(Arrays.asList(SpringTool.getBasePackageNames()));
        if (AutoConfigurationPackages.has(applicationContext)) {
            packages.addAll(AutoConfigurationPackages.get(applicationContext));
        }

        List<Class<?>> result = new ArrayList<>();
        for (String pkg : packages) {
            if (pkg == null || pkg.isBlank()) {
                continue;
            }
            try {
                result.addAll(ClassTool.scanPackageBySuper(pkg, Job.class));
            } catch (Exception e) {
                log.warn("扫描定时任务失败，package={}：{}", pkg, e.getMessage());
            }
        }

        return result.stream()
                .filter(JobDefinitionScanner::isRegistrable)
                .distinct()
                .sorted(Comparator.comparing(Class::getName))
                .toList();
    }

    static boolean isRegistrable(Class<?> cls) {
        if (cls == null || cls.isInterface() || cls.isAnnotation() || cls.isEnum()) {
            return false;
        }
        if (cls.isAnonymousClass() || cls.isLocalClass()) {
            return false;
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            return false;
        }
        return BaseJob.class.isAssignableFrom(cls) || cls.getAnnotation(JobDescription.class) != null;
    }
}
