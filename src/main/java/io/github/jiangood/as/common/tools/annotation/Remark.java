package io.github.jiangood.as.common.tools.annotation;

import java.lang.annotation.*;

/**
 * 字段显示值， 负责备注，可用于很多
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Remark {

    String value();


}
