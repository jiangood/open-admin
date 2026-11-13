package io.github.jiangood.openadmin.modules.logviewer.perm;

import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
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
     * 静态前缀 -> 权限映射。适用于「同一前缀下所有日志共用一个权限码」的场景。
     *
     * @return key 前缀 -> 所需权限，如 {@code {"job": "job:read"}}
     */
    default Map<String, String> logKeyPermissions() {
        return Collections.emptyMap();
    }

    /**
     * 动态解析某个具体 key 所需的权限码；仅当该 provider 负责此 key 时返回非 null。
     * <p>
     * 用于权限随具体日志记录而异的场景（如任务日志按记录的 permission 字段鉴权）。
     * 返回 null 表示不处理，交由静态映射或其他 provider。
     * <p>
     * provider 也可以在此方法内直接抛出 {@link AccessDeniedException} 来拒绝访问
     * （例如记录不存在时返回语义化拒绝）。
     *
     * @param key 归一化后的日志 key（无前导 /），如 {@code task/xxx}
     * @return 所需权限码，或 null 表示不负责
     */
    default String requiredPermission(String key) {
        return null;
    }
}
