package io.github.jiangood.openadmin.modules.logviewer.perm;

import io.github.jiangood.openadmin.framework.auth.LoginTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.jiangood.openadmin.framework.MessageConst.MSG_FORBIDDEN;

/**
 * 日志 key 权限注册表：按 key 前缀决定查看该日志需要什么权限。
 * <p>
 * 例：{@code job/{logId}} 由定时任务模块注册为 {@code job:read}；
 * {@code task/{logId}} 由任务模块按记录的 permission 字段动态鉴权。
 * 未注册且无 provider 负责的 key 一律拒绝，避免通用日志接口变成任意文件读取。
 */
@Slf4j
@Component
public class LogKeyPermissionRegistry {

    /**
     * 前缀 -> 权限（静态映射）
     */
    private final Map<String, String> prefixPermissions = new HashMap<>();

    private final List<LogKeyPermissionProvider> providers;

    public LogKeyPermissionRegistry(List<LogKeyPermissionProvider> providers) {
        this.providers = providers;
        for (LogKeyPermissionProvider provider : providers) {
            Map<String, String> entries = provider.logKeyPermissions();
            if (entries == null) {
                continue;
            }
            entries.forEach(this::register);
        }
        log.info("日志 key 权限映射已加载: {}", prefixPermissions);
    }

    /**
     * 注册 key 前缀对应的权限，重复注册以后者为准。
     */
    public void register(String prefix, String permission) {
        if (prefix == null || prefix.isBlank() || permission == null || permission.isBlank()) {
            return;
        }
        prefixPermissions.put(stripSlash(prefix), permission);
    }

    /**
     * 解析查看该 key 所需权限；未注册返回 null（仅静态映射）。
     */
    public String resolvePermission(String key) {
        String normalized = stripSlash(key);
        if (normalized == null) {
            return null;
        }
        String matched = null;
        int matchedLength = -1;
        for (Map.Entry<String, String> entry : prefixPermissions.entrySet()) {
            String prefix = entry.getKey();
            if (normalized.equals(prefix) || normalized.startsWith(prefix + "/")) {
                // 取最长前缀，允许子模块覆盖更细粒度的路径
                if (prefix.length() > matchedLength) {
                    matched = entry.getValue();
                    matchedLength = prefix.length();
                }
            }
        }
        return matched;
    }

    /**
     * 校验当前登录用户是否有权查看该日志，无权时抛 {@link AccessDeniedException}。
     */
    public void checkPermission(String key) {
        String normalized = stripSlash(key);
        if (normalized == null) {
            throw new AccessDeniedException(MSG_FORBIDDEN + "：未配置该日志的查看权限");
        }

        // 1) 动态 provider（可按具体 key 决定权限，或直接拒绝）
        for (LogKeyPermissionProvider provider : providers) {
            String dynamic = provider.requiredPermission(normalized);
            if (dynamic != null) {
                if (LogKeyPermissionProvider.LOGIN_REQUIRED.equals(dynamic)) {
                    assertAuthenticated();
                } else {
                    assertHasPermission(dynamic);
                }
                return;
            }
        }

        // 2) 静态前缀映射
        String permission = resolvePermission(normalized);
        if (permission == null) {
            log.warn("日志 key 未注册权限，拒绝访问: {}", key);
            throw new AccessDeniedException(MSG_FORBIDDEN + "：未配置该日志的查看权限");
        }
        assertHasPermission(permission);
    }

    private void assertAuthenticated() {
        // getPermissions() 在已登录时返回权限集合（可为空），未登录/无上下文时按实现可能为 null
        if (LoginTool.getPermissions() == null) {
            throw new AccessDeniedException(MSG_FORBIDDEN);
        }
    }

    private void assertHasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            throw new AccessDeniedException(MSG_FORBIDDEN);
        }
        List<String> permissions = LoginTool.getPermissions();
        if (permissions == null) {
            throw new AccessDeniedException(MSG_FORBIDDEN + "：" + permission);
        }
        if (permissions.contains("*") || permissions.contains(permission)) {
            return;
        }
        throw new AccessDeniedException(MSG_FORBIDDEN + "：" + permission);
    }

    private static String stripSlash(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }
}
