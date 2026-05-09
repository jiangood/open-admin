package io.github.jiangood.openadmin.util;

import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReflectionTool {

    /**
     * 获取指定类的所有getter方法, 包括is如 isEnabled
     *
     * @param clazz 要解析的类对象
     * @return 包含该类所有getter方法的列表，排除"class"属性的getter方法
     */
    public static List<Method> getGetters(Class<?> clazz) {
        // 获取所有属性描述符
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(clazz);

        List<Method> getters = new ArrayList<>(descriptors.length);

        for (PropertyDescriptor descriptor : descriptors) {
            if ("class".equals(descriptor.getName())) {
                continue;
            }

            Method getter = descriptor.getReadMethod();
            if (getter != null) {
                getters.add(getter);
            }
        }

        return getters;
    }

    public static List<PropertyDescriptor> getProperties(Class<?> clazz) {
        // 获取所有属性描述符
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(clazz);

        List<PropertyDescriptor> list = new ArrayList<>(descriptors.length);

        for (PropertyDescriptor descriptor : descriptors) {
            if ("class".equals(descriptor.getName())) {
                continue;
            }
            list.add( descriptor);
        }

        return list;
    }



    /**
     * 获取首个泛型类型
     *
     * @param propertyDescriptor
     * @return
     */
    public static Type getFirstGeneric(PropertyDescriptor propertyDescriptor) {
        Method readMethod = propertyDescriptor.getReadMethod();
        Type genericReturnType = readMethod.getGenericReturnType();

        if (genericReturnType instanceof ParameterizedType pt) {

            Type argument = pt.getActualTypeArguments()[0];

            return argument;
        }

        return null;
    }
}
