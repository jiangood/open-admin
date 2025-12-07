package io.admin.framework.config.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * 自定义权限检查注解，仅支持检查单个权限字符串。
 * * 关键 SpEL 表达式解释:
 * - hasAuthority(#p0) : #p0 代表注解的第一个参数 (即 value() 的 String 值)。
 * - 这种方式直接调用了 Spring Security 内置的 hasAuthority() 函数，非常简洁。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("hasAuthority(#p0)") // 【核心】直接调用内置函数
public @interface HasPermission {

    /**
     * 需要检查的单个权限字符串，例如 "product:write"
     */
    String value();
}
