package io.github.jiangood.openadmin.framework.log;

import java.lang.annotation.*;

/**
 * 简化后的自定义日志注解：用于标记需要记录方法执行日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log { // 注解名改为 Log
    String value();


}
