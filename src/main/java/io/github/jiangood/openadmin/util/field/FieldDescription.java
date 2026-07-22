package io.github.jiangood.openadmin.util.field;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})


@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface FieldDescription {

    String name() default "";

    boolean required() default true;

    String label();

    /**
     * 占位提示
     *
     * @return
     */
    String placeholder() default "";

    int len() default -1;

    /**
     * 默认值
     *
     * @return
     */
    String defaultValue() default "";

    /**
     * 字段类型
     *
     * @return
     */
    ValueType type() default ValueType.STRING;


}
