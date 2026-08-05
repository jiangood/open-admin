package io.github.jiangood.openadmin.framework.dict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在枚举常量上，声明该字典项的文本（label）与可选颜色（color）。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictItem {
    String label();
    String color() default "";
}
