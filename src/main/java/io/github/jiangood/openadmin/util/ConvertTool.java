package io.github.jiangood.openadmin.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.TypeReference;

import java.lang.reflect.Type;
import java.util.List;

public class ConvertTool {
    private ConvertTool() {
    }


    /**
     * 转换器
     * 基于 hutool
     *
     * @param type
     * @param value
     * @param <T>
     * @param genericTypes 泛型
     * @return
     */
    public static <T> T convert(Class<T> type, Object value, Type... genericTypes) {
        if (value == null) {
            return null;
        }

        // 处理空字符串的情况
        if (value instanceof String s && s.trim().isEmpty()) {
            return null;
        }

        // 修复数字转枚举时，输入为long的异常（常见于数据取值）
        if (Enum.class.isAssignableFrom(type) && value instanceof Long l) {
            value = l.intValue();
        }

        T listResult = convertList(type, value, genericTypes);
        if (listResult != null) {
            return listResult;
        }

        // 特殊处理数值范围检查
        if (!numericParseable(type, value)) {
            return null;
        }

        try {
            return Convert.convert(type, value);
        } catch (Exception e) {
            // 处理转换失败的情况，返回null
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertList(Class<T> type, Object value, Type[] genericTypes) {
        if (!type.isAssignableFrom(List.class) || genericTypes == null || genericTypes.length != 1) {
            return null;
        }
        if (genericTypes[0] != Integer.class) {
            return null;
        }
        TypeReference<List<Integer>> typeRef = new TypeReference<>() {
        };
        return (T) Convert.convert(typeRef, value);
    }

    private static boolean numericParseable(Class<?> type, Object value) {
        if (!(value instanceof String str)) {
            return true;
        }
        try {
            if (type == Byte.class) Byte.parseByte(str);
            else if (type == Short.class) Short.parseShort(str);
            else if (type == Integer.class) Integer.parseInt(str);
            else if (type == Long.class) Long.parseLong(str);
            else if (type == Float.class) Float.parseFloat(str);
            else if (type == Double.class) Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}
