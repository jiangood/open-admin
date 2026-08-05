package io.github.jiangood.openadmin.framework.dict;

import java.lang.reflect.Field;

public final class DictEnumTool {

    private DictEnumTool() {
    }

    public static String getLabel(Enum<?> constant) {
        return dictItemOf(constant).label();
    }

    public static String getColor(Enum<?> constant) {
        String color = dictItemOf(constant).color();
        return color.isEmpty() ? null : color;
    }

    private static DictItem dictItemOf(Enum<?> constant) {
        try {
            Field field = constant.getDeclaringClass().getDeclaredField(constant.name());
            DictItem item = field.getAnnotation(DictItem.class);
            if (item == null) {
                throw new IllegalArgumentException(constant.getDeclaringClass().getSimpleName()
                        + "." + constant.name() + " 缺少 @DictItem 注解");
            }
            return item;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("读取枚举 @DictItem 失败", e);
        }
    }
}
