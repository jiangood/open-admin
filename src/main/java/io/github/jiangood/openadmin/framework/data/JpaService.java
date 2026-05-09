package io.github.jiangood.openadmin.framework.data;

import io.github.jiangood.openadmin.util.SpringTool;
import jakarta.persistence.Entity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class JpaService {

    private static final String CACHE_NAME = "jpaEntityNames";
    private static final String CACHE_KEY = "ALL_NAMES";

    private final CacheManager cacheManager;

    public JpaService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private static List<String> findBySuperClass(Class baseClas) {
        try {

            String base = ClassUtils.convertClassNameToResourcePath(baseClas.getPackage().getName());
            String locationPattern = "classpath*:" + base + "/**/*.class";

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            org.springframework.core.io.Resource[] resources = resolver.getResources(locationPattern);


            MetadataReaderFactory readerfactory = new CachingMetadataReaderFactory(resolver);

            List<String> list = new ArrayList<>();
            for (org.springframework.core.io.Resource resource : resources) {
                MetadataReader meta = readerfactory.getMetadataReader(resource);
                if (meta.getAnnotationMetadata().hasAnnotation(Entity.class.getName())) {

                    ClassMetadata classMetadata = meta.getClassMetadata();
                    list.add(classMetadata.getClassName());
                }
            }
            return list;

        } catch (Exception e) {
            log.error("查找实体类失败", e);
            return Collections.emptyList();
        }
    }

    public <T> Class<T> findOne(String name) throws IOException, ClassNotFoundException {
        List<String> list = findAllNames();
        for (String clsName : list) {
            String simpleName = StringUtils.substringAfterLast(clsName, ".");
            if (simpleName.equalsIgnoreCase(name)) {
                return (Class<T>) Class.forName(clsName);
            }

        }
        return null;
    }

    public List<Class<?>> findAllClass() throws IOException, ClassNotFoundException {
        List<Class<?>> clsList = new ArrayList<>();
        List<String> allNames = findAllNames();
        for (String name : allNames) {
            Class<?> cls = Class.forName(name);
            clsList.add(cls);
        }
        return clsList;
    }

    public List<String> findAllNames() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            List<String> cached = cache.get(CACHE_KEY, List.class);
            if (cached != null) {
                return cached;
            }
        }

        Set<Class<?>> basePackageClasses = SpringTool.getBasePackageClasses();
        List<String> entityList = new LinkedList<>();
        for (Class<?> cls : basePackageClasses) {
            List<String> pkgEntityList = findBySuperClass(cls);
            entityList.addAll(pkgEntityList);
        }
        Collections.sort(entityList);

        if (cache != null) {
            cache.put(CACHE_KEY, entityList);
        }

        return entityList;
    }

}
