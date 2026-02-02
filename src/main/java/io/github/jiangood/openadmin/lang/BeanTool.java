package io.github.jiangood.openadmin.lang;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

/**
 * Bean工具类，提供Bean操作相关的工具方法
 */
@Slf4j
public class BeanTool {

    /**
     * 通过无参构造器创建类的实例
     *
     * @param cls 类对象
     * @param <T> 泛型类型
     * @return 类的实例
     * @throws RuntimeException 如果创建实例失败
     */
    public static <T> T newInstance(Class<T> cls) {
        try {
            return cls.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将源对象的属性复制到目标对象
     *
     * @param source 源对象
     * @param target 目标对象
     * @param <T> 目标对象类型
     * @return 目标对象
     * @throws BeansException 如果复制失败
     */
    public static <T> T copy(Object source, T target) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * 将两个源对象的属性复制到目标对象
     *
     * @param source1 第一个源对象
     * @param source2 第二个源对象
     * @param target 目标对象
     * @param <T> 目标对象类型
     * @return 目标对象
     * @throws BeansException 如果复制失败
     */
    public static <T> T copy(Object source1, Object source2, T target) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source1, target);
        org.springframework.beans.BeanUtils.copyProperties(source2, target);
        return target;
    }


    /**
     * 将三个源对象的属性复制到目标对象
     *
     * @param source1 第一个源对象
     * @param source2 第二个源对象
     * @param source3 第三个源对象
     * @param target 目标对象
     * @param <T> 目标对象类型
     * @return 目标对象
     * @throws BeansException 如果复制失败
     */
    public static <T> T copy(Object source1, Object source2, Object source3, T target) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source1, target);
        org.springframework.beans.BeanUtils.copyProperties(source2, target);
        org.springframework.beans.BeanUtils.copyProperties(source3, target);
        return target;
    }


    /**
     * 将源列表转换为目标列表（已废弃，建议使用copyToList方法）
     *
     * @param sourceList 源列表
     * @param converter 转换函数
     * @param <T> 源类型
     * @param <R> 目标类型
     * @return 目标列表
     */
    @Deprecated
    public static <T, R> List<R> convertList(Iterable<T> sourceList, Function<T, R> converter) {
        return copyToList(sourceList, converter);
    }

    /**
     * 将源列表转换为目标列表
     *
     * @param sourceList 源列表
     * @param converter 转换函数
     * @param <T> 源类型
     * @param <R> 目标类型
     * @return 目标列表
     */
    public static <T, R> List<R> copyToList(Iterable<T> sourceList, Function<T, R> converter) {
        List<R> list = new ArrayList<>();
        for (T s : sourceList) {
            R apply = converter.apply(s);
            list.add(apply);
        }
        return list;

    }

    /**
     * 将源对象列表转换为Map列表
     *
     * @param sourceList 源对象列表
     * @param cls 源对象类型
     * @param ignoreProperties 要忽略的属性
     * @param <T> 源对象类型
     * @return Map列表
     */
    public static <T> List<Map<String, Object>> copyToListMap(Iterable<T> sourceList, Class<T> cls, String... ignoreProperties) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object bean : sourceList) {
            Map<String, Object> map = copyToMap(cls, bean, ignoreProperties);
            list.add(map);
        }
        return list;
    }

    /**
     * 将对象转换为Map
     *
     * @param cls 对象类型
     * @param bean 源对象
     * @param ignoreProperties 要忽略的属性
     * @param <T> 源对象类型
     * @return 转换后的Map
     */
    public static <T> Map<String, Object> copyToMap(Class<T> cls, Object bean, String... ignoreProperties) {
        List<String> ignoreList = Arrays.asList(ignoreProperties);

        Map<String, Object> map = new HashMap<>();

        if (bean == null) {
            return map;
        }

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
                if (!name.startsWith("get")) {
                    continue;
                }

                String k = StringTool.removePreAndLowerFirst(name, "get");
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
