package io.github.jiangood.openadmin.framework.dict;

import io.github.jiangood.openadmin.framework.enums.StatusColor;

import java.lang.reflect.Field;

public final class DictEnumTool {

    private DictEnumTool() {
    }

    public static String getLabel(Enum<?> constant) {
        return dictItemOf(constant).label();
    }

    public static StatusColor getColor(Enum<?> constant) {
        String color = dictItemOf(constant).color();
        if (color.isEmpty()) {
            return null;
        }
        try {
            return StatusColor.valueOf(color);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(constant.getDeclaringClass().getSimpleName() + "."
                    + constant.name() + " 的 @DictItem.color 不是合法的 StatusColor: " + color, e);
        }
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
