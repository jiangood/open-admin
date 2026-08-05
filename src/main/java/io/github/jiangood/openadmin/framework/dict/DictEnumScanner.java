package io.github.jiangood.openadmin.framework.dict;

import io.github.jiangood.openadmin.OpenAdminConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class DictEnumScanner {

    private final ObjectProvider<BeanFactory> beanFactoryProvider;

    public DictEnumScanner(ObjectProvider<BeanFactory> beanFactoryProvider) {
        this.beanFactoryProvider = beanFactoryProvider;
    }

    public List<Class<? extends Enum<?>>> scan() {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return true;
                    }
                };
        provider.addIncludeFilter(new AnnotationTypeFilter(DictType.class));

        Set<String> classNames = new LinkedHashSet<>();
        for (String basePackage : basePackages()) {
            provider.findCandidateComponents(basePackage)
                    .forEach(bd -> {
                        if (bd.getBeanClassName() != null) {
                            classNames.add(bd.getBeanClassName());
                        }
                    });
        }

        List<Class<? extends Enum<?>>> result = new ArrayList<>();
        for (String className : classNames) {
            try {
                Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
                if (clazz.isEnum() && clazz.getEnclosingClass() == null) {
                    result.add((Class<? extends Enum<?>>) clazz);
                } else {
                    log.warn("{} 标注了 @DictType 但不是顶层枚举，忽略", className);
                }
            } catch (ClassNotFoundException | LinkageError e) {
                log.warn("扫描 @DictType 枚举失败：{}", className, e);
            }
        }
        return result;
    }

    private List<String> basePackages() {
        Set<String> packages = new LinkedHashSet<>();
        packages.add(OpenAdminConfiguration.PKG);
        try {
            packages.addAll(AutoConfigurationPackages.get(beanFactoryProvider.getObject()));
        } catch (IllegalStateException e) {
            log.warn("AutoConfigurationPackages 不可用，仅扫描框架基础包 {}", OpenAdminConfiguration.PKG);
        }
        return List.copyOf(packages);
    }
}
