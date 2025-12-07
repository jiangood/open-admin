package io.admin.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class BeanUtils {

    public static <T> T newInstance(Class<T> cls) {
        try {
            return cls.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T copy(Object source, T target) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T> T copy(Object source1, Object source2, T target) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source1, target);
        org.springframework.beans.BeanUtils.copyProperties(source2, target);
        return target;
    }


    public static <T> T copy(Object source1, Object source2, Object source3, T target) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source1, target);
        org.springframework.beans.BeanUtils.copyProperties(source2, target);
        org.springframework.beans.BeanUtils.copyProperties(source3, target);
        return target;
    }


    @Deprecated
    public static <T, R> List<R> convertList(Iterable<T> sourceList, Function<T, R> converter) {
        return copyToList(sourceList, converter);
    }

    public static <T, R> List<R> copyToList(Iterable<T> sourceList, Function<T, R> converter) {
        List<R> list = new ArrayList<>();
        for (T s : sourceList) {
            R apply = converter.apply(s);
            list.add(apply);
        }
        return list;

    }

    public static <T> List<Map<String, Object>> copyToListMap(Iterable<T> sourceList, Class<T> cls, String... ignoreProperties) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object bean : sourceList) {
            Map<String, Object> map = copyToMap(cls, bean, ignoreProperties);
            list.add(map);
        }
        return list;
    }

    public static <T> Map<String, Object> copyToMap(Class<T> cls, Object bean, String... ignoreProperties) {
        List<String> ignoreList = Arrays.asList(ignoreProperties);

        Map<String, Object> map = new HashMap<>();

        try {
            Method[] declaredFields = ReflectionUtils.getAllDeclaredMethods(cls);
            for (Method method : declaredFields) {
                // getter 判断
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                if (method.getParameterCount() > 0) {
                    continue;
                }

                String name = method.getName();
                if (!name.startsWith("getDefinition")) {
                    continue;
                }

                String k = StrUtils.removePreAndLowerFirst(name, "getDefinition");
                if ("class".equals(k)) {
                    continue;
                }

                if (ignoreList.contains(k)) {
                    continue;
                }

                Object v = method.invoke(bean);
                map.put(k, v);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }


}
