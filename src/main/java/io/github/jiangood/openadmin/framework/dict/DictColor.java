package io.github.jiangood.openadmin.framework.dict;

import io.github.jiangood.openadmin.framework.enums.StatusColor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在枚举常量上，声明该字典项的颜色。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictColor {
    StatusColor value();
}
