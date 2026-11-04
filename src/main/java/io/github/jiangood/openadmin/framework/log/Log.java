package io.github.jiangood.openadmin.framework.log;

import java.lang.annotation.*;

/**
 * 简化后的自定义日志注解：用于标记需要记录方法执行日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log { // 注解名改为 Log

    /** 操作名称，如「用户-创建」 */
    String value();

    /**
     * 操作详情模板，支持 {@code ${...}} 占位符，占位符内为 SpEL 表达式，
     * 变量为方法参数（按参数名，或 {@code #p0} / {@code #a0} 形式的索引）。
     * <p>
     * 示例：{@code detail = "新增用户「${#input.name}」"}。
     * <p>
     * 求值失败或结果为 null 时按空串处理，不会影响业务方法；
     * 不要在此拼接密码等敏感字段。
     */
    String detail() default "";
}
