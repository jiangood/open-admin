package io.github.jiangood.openadmin.framework.dict;

import io.github.jiangood.openadmin.OpenAdminConfiguration;
import io.github.jiangood.openadmin.framework.spi.StartupHook;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.entity.SysDictType;
import io.github.jiangood.openadmin.modules.system.repository.SysDictItemRepository;
import io.github.jiangood.openadmin.modules.system.repository.SysDictTypeRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class DictSeedSync implements StartupHook {
    
    private static final String ROOT_ID = "1";

    private final ObjectProvider<BeanFactory> beanFactoryProvider;

    private final ObjectProvider<SysDictTypeRepository> typeRepositoryProvider;

    private final ObjectProvider<SysDictItemRepository> itemRepositoryProvider;

    // ---------- 枚举 → 字典同步 ----------

    @Override
    @Transactional
    public void afterSeedDataInitialize() {
        List<Class<? extends Enum<?>>> enumClasses = scan();
        if (enumClasses.isEmpty()) {
            return;
        }
        SysDictTypeRepository typeRepository = typeRepositoryProvider.getObject();
        SysDictItemRepository itemRepository = itemRepositoryProvider.getObject();

        int typeCount = 0;
        int itemCount = 0;
        for (Class<? extends Enum<?>> enumClass : enumClasses) {
            DictType dictType = enumClass.getAnnotation(DictType.class);
            syncType(typeRepository, dictType, ROOT_ID);
            typeCount++;
            itemCount += syncItems(itemRepository, enumClass, dictType.code());
        }
        log.info("字典枚举同步完成：{} 个类型，{} 个字典项", typeCount, itemCount);
    }


    private void syncType(SysDictTypeRepository typeRepository, DictType dictType, String rootId) {
        typeRepository.findByTypeCode(dictType.code())
                .ifPresentOrElse(existing -> {
                    if (!dictType.label().equals(existing.getTypeLabel())) {
                        existing.setTypeLabel(dictType.label());
                        typeRepository.save(existing);
                    }
                }, () -> {
                    SysDictType type = new SysDictType();
                    type.setPid(rootId);
                    type.setTypeCode(dictType.code());
                    type.setTypeLabel(dictType.label());
                    type.setEnabled(true);
                    type.setSeq(0);
                    typeRepository.save(type);
                });
    }

    private int syncItems(SysDictItemRepository itemRepository, Class<? extends Enum<?>> enumClass, String typeCode) {
        int count = 0;
        Object[] constants = enumClass.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            int seq = i;
            Enum<?> constant = (Enum<?>) constants[i];
            String code = constant.name();
            String label = getLabel(constant);
            String color = getColor(constant);
            itemRepository.findByTypeCodeAndCode(typeCode, code)
                    .ifPresentOrElse(item -> mergeItem(itemRepository, item, label, color, seq),
                            () -> insertItem(itemRepository, typeCode, code, label, color, seq));
            count++;
        }
        return count;
    }

    private void mergeItem(SysDictItemRepository itemRepository, SysDictItem item, String label, String color, int seq) {
        boolean changed = false;
        if (!label.equals(item.getLabel())) { item.setLabel(label); changed = true; }
        if (!Objects.equals(color, item.getColor())) { item.setColor(color); changed = true; }
        if (!Boolean.TRUE.equals(item.getEnabled())) { item.setEnabled(true); changed = true; }
        if (!Objects.equals(seq, item.getSeq())) { item.setSeq(seq); changed = true; }
        if (changed) itemRepository.save(item);
    }

    private void insertItem(SysDictItemRepository itemRepository, String typeCode, String code, String label, String color, int seq) {
        SysDictItem item = new SysDictItem();
        item.setTypeCode(typeCode);
        item.setCode(code);
        item.setLabel(label);
        item.setColor(color);
        item.setEnabled(true);
        item.setSeq(seq);
        itemRepository.save(item);
    }

    // ---------- @DictType 枚举自动发现 ----------

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

    // ---------- @DictItem 读取 ----------

    public static String getLabel(Enum<?> constant) {
        return dictItemOf(constant).label();
    }

    public static String getColor(Enum<?> constant) {
        String color = dictItemOf(constant).color();
        return color.isEmpty() ? null : color;
    }

    private static DictItem dictItemOf(Enum<?> constant) {
        try {
            Field field = constant.getDeclaringClass().getDeclaredField(constant.name());
            DictItem item = field.getAnnotation(DictItem.class);
            if (item == null) {
                throw new IllegalArgumentException(constant.getDeclaringClass().getSimpleName()
                        + "." + constant.name() + " 缺少 @DictItem 注解");
            }
            return item;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("读取枚举 @DictItem 失败", e);
        }
    }
}
