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
 * 例：{@code job/{logId}} 由定时任务模块注册为 {@code job:read}。
 * 未注册的前缀一律拒绝（而不是放行），避免通用日志接口变成任意文件读取。
 */
@Slf4j
@Component
public class LogKeyPermissionRegistry {

    /**
     * 前缀 -> 权限
     */
    private final Map<String, String> prefixPermissions = new HashMap<>();

    public LogKeyPermissionRegistry(List<LogKeyPermissionProvider> providers) {
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
     * 注册 key 前缀对应的权限，重复注册以后者为准
     */
    public void register(String prefix, String permission) {
        if (prefix == null || prefix.isBlank() || permission == null || permission.isBlank()) {
            return;
        }
        prefixPermissions.put(stripSlash(prefix), permission);
    }

    /**
     * 解析查看该 key 所需权限；未注册返回 null
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
     * 校验当前登录用户是否有权查看该日志，无权时抛出 {@link AccessDeniedException}
     */
    public void checkPermission(String key) {
        String permission = resolvePermission(key);
        if (permission == null) {
            log.warn("日志 key 未注册权限，拒绝访问: {}", key);
            throw new AccessDeniedException(MSG_FORBIDDEN + "：未配置该日志的查看权限");
        }

        List<String> permissions = LoginTool.getPermissions();
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
