package io.github.jiangood.openadmin.util.annotation;

import io.github.jiangood.openadmin.util.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Slf4j
public class RemarkTool {
    private RemarkTool() {
    }


    public static String getRemark(Field field) {
        if (field == null) {
            return null;
        }
        Remark annotation = field.getAnnotation(Remark.class);
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }

    public static String getRemark(Class<?> t) {
        if (t == null) {
            return null;
        }
        Remark annotation = t.getAnnotation(Remark.class);
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }

    public static String getRemark(Enum<?> t) {
        if (t == null) {
            return null;
        }
        try {
            Field f = t.getClass().getDeclaredField(t.name());
            Remark ann = f.getAnnotation(Remark.class);
            if (ann == null) {
                throw new BusinessException(t.getClass().getSimpleName() + "没有设置注解@Remark");
            }
            return ann.value();
        } catch (NoSuchFieldException | SecurityException e) {
            log.error("获取枚举Remark注解失败", e);
        }
        return null;
    }

    public static String getRemark(Method method) {
        if (method == null) {
            return null;
        }
        Remark ann = method.getAnnotation(Remark.class);
        return ann == null ? null : ann.value();
    }
}
