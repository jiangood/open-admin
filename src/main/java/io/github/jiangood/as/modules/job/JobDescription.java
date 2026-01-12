package io.github.jiangood.as.modules.job;


import io.github.jiangood.as.common.tools.field.FieldDescription;

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
