package io.github.jiangood.openadmin.modules.logviewer.perm;

import java.util.Map;

/**
 * 日志 key 前缀与所需权限的映射提供者。
 * <p>
 * 日志查看是通用能力，具体日志属于哪个业务模块、需要什么权限，由各业务模块自己声明，
 * 避免通用模块把权限写死成某一个业务的权限（如 {@code job:read}）。
 * <p>
 * 实现为 Spring Bean 即自动生效。
 */
public interface LogKeyPermissionProvider {

    /**
     * @return key 前缀 -> 所需权限，如 {@code {"job": "job:read"}}
     */
    Map<String, String> logKeyPermissions();
}
