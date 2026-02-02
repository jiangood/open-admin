package io.github.jiangood.openadmin.lang;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.TypeReference;

import java.lang.reflect.Type;
import java.util.List;

public class ConvertTool {

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
        if (value instanceof String && ((String) value).trim().isEmpty()) {
            return null;
        }

        // 修复数字转枚举时，输入为long的异常（常见于数据取值）
        if (Enum.class.isAssignableFrom(type) && value instanceof Long) {
            value = ((Long) value).intValue();
        }

        if (type.isAssignableFrom(List.class) && genericTypes != null && genericTypes.length == 1) {
            Type genType = genericTypes[0];

            if (genType == Integer.class) {
                TypeReference<List<Integer>> typeRef = new TypeReference<>() {
                };
                List<Integer> list = Convert.convert(typeRef, value);
                return (T) list;
            }
        }

        try {
            // 特殊处理数值范围检查
            if (type == Byte.class && value instanceof String) {
                try {
                    Byte.parseByte((String) value);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Short.class && value instanceof String) {
                try {
                    Short.parseShort((String) value);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Integer.class && value instanceof String) {
                try {
                    Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Long.class && value instanceof String) {
                try {
                    Long.parseLong((String) value);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Float.class && value instanceof String) {
                try {
                    Float.parseFloat((String) value);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Double.class && value instanceof String) {
                try {
                    Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            T result = Convert.convert(type, value);
            return result;
        } catch (Exception e) {
            // 处理转换失败的情况，返回null
            return null;
        }
    }


}
