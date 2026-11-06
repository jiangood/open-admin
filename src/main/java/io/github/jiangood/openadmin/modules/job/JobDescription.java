package io.github.jiangood.openadmin.modules.job;


import io.github.jiangood.openadmin.modules.job.entity.JobLogAggregation;
import io.github.jiangood.openadmin.util.field.FieldDescription;

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

    /**
     * 自动注册时使用的默认 cron；为空表示只补建任务记录，不启用调度
     */
    String defaultCron() default "";

    /**
     * 自动注册时是否默认启用；启用时必须同时声明 {@link #defaultCron()}
     */
    boolean defaultEnabled() default false;

    /**
     * 自动注册时写入的执行记录方式
     */
    JobLogAggregation logAggregation() default JobLogAggregation.PER_RUN;

    /**
     * 是否参与启动时自动注册；设为 false 可避免依赖库中的任务出现在所有业务系统中
     */
    boolean autoRegister() default true;

}
