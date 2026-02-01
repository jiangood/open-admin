package io.github.jiangood.openadmin.framework.perm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 *  简化 security 的权限注解
 *  @HasPermission("job:read" )  相当于  @PreAuthorize("hasAuthority('job:read')")
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface HasPermission {

    String value();
}
