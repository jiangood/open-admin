package io.github.jiangood.openadmin.framework.dict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在枚举类型上，声明该枚举对应一个数据字典类型。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictType {
    String code();
    String label();
}
