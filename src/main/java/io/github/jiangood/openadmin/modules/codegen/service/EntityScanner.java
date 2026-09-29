package io.github.jiangood.openadmin.modules.codegen.service;

import cn.hutool.core.util.ClassUtil;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.modules.codegen.dto.EntityMetaVO;
import io.github.jiangood.openadmin.util.SpringTool;
import jakarta.persistence.Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态扫描已注册实体（继承 BaseEntity 且标注 @Entity）。
 * <p>
 * 扫描范围复用 {@link SpringTool#getBasePackageClasses()}：框架包 + 业务 @SpringBootApplication/@ComponentScan 包。
 */
@Slf4j
@Component
public class EntityScanner {

    private final Map<String, EntityMetaVO> cache = new ConcurrentHashMap<>();

    public List<EntityMetaVO> scan() {
        Set<Class<?>> classes = new LinkedHashSet<>();
        for (Class<?> baseClass : SpringTool.getBasePackageClasses()) {
            String basePackage = baseClass.getPackageName();
            try {
                classes.addAll(ClassUtil.scanPackageBySuper(basePackage, BaseEntity.class));
            } catch (Exception e) {
                log.warn("扫描实体失败：{}", basePackage, e);
            }
        }

        List<EntityMetaVO> result = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            if (!clazz.isAnnotationPresent(Entity.class)) {
                continue;
            }
            EntityMetaVO meta = EntityMetaFactory.build(clazz);
            cache.put(meta.getClassName(), meta);
            result.add(meta);
        }

        result.sort(Comparator
                .comparingInt((EntityMetaVO m) -> m.isFrameworkEntity() ? 1 : 0)
                .thenComparing(EntityMetaVO::getSimpleName));
        return result;
    }

    /**
     * 按类名获取实体元数据，命中缓存；未命中时重新扫描。类名必须来自扫描结果。
     */
    public EntityMetaVO get(String className) {
        EntityMetaVO meta = cache.get(className);
        if (meta == null) {
            scan();
            meta = cache.get(className);
        }
        Assert.notNull(meta, "实体不存在：" + className);
        return meta;
    }
}
