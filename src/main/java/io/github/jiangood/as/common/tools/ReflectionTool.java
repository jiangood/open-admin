package io.github.jiangood.as.common.tools;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionTool {

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
