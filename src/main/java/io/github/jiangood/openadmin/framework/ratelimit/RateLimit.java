package io.github.jiangood.openadmin.framework.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流，基于滑动窗口 + IP
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 窗口内最大请求数
     */
    int count() default 100;

    /**
     * 窗口时间（秒）
     */
    int duration() default 60;
}
