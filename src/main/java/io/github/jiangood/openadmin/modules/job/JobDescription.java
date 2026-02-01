package io.github.jiangood.openadmin.modules.job;


import io.github.jiangood.openadmin.lang.field.FieldDescription;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JobDescription {

    String label();


    FieldDescription[] params() default {};

    /**
     * 动态参数
     *
     * @return
     */
    Class<? extends JobParamFieldProvider> paramsProvider() default JobParamFieldProvider.class;

}
