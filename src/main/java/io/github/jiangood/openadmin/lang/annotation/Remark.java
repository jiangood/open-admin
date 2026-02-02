package io.github.jiangood.openadmin.lang.annotation;

import java.lang.annotation.*;

/**
 * 字段显示值， 负责备注，可用于很多
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
public @interface Remark {

    String value();


}
