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

        if (type.isAssignableFrom(List.class) && genericTypes != null && genericTypes.length == 1) {
            Type genType = genericTypes[0];

            if (genType == Integer.class) {
                TypeReference<List<Integer>> typeRef = new TypeReference<>() {
                };
                List<Integer> list = Convert.convert(typeRef, value);
                @SuppressWarnings("unchecked")
                T result = (T) list;
                return result;
            }
        }

        try {
            // 特殊处理数值范围检查
            if (type == Byte.class && value instanceof String s1) {
                try {
                    Byte.parseByte(s1);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Short.class && value instanceof String s2) {
                try {
                    Short.parseShort(s2);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Integer.class && value instanceof String s3) {
                try {
                    Integer.parseInt(s3);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Long.class && value instanceof String s4) {
                try {
                    Long.parseLong(s4);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Float.class && value instanceof String s5) {
                try {
                    Float.parseFloat(s5);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else if (type == Double.class && value instanceof String s6) {
                try {
                    Double.parseDouble(s6);
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            return Convert.convert(type, value);
        } catch (Exception e) {
            // 处理转换失败的情况，返回null
            return null;
        }
    }


}
