package io.github.jiangood.openadmin.lang;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ClassTool {

    private static final CachingMetadataReaderFactory cachingMetadataReaderFactory = new CachingMetadataReaderFactory();

    public static <T> Set<Class<T>> scanPackageBySuper(String basePackage, Class<T> javaDelegateClass) throws IOException, ClassNotFoundException {
        Set<Class<T>> ret = new HashSet<>();

        String pkgPath = basePackage.replace(".", "/");

        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:" + pkgPath + "/**/*.class");
        ClassLoader loader = ClassLoader.getSystemClassLoader();

        for (Resource resource : resources) {
            MetadataReader reader = cachingMetadataReaderFactory.getMetadataReader(resource);
            String className = reader.getClassMetadata().getClassName();
            Class<T> cls = (Class<T>) loader.loadClass(className);

            if (javaDelegateClass.isAssignableFrom(cls)) {
                ret.add(cls);
            }
        }

        return ret;
    }


}
