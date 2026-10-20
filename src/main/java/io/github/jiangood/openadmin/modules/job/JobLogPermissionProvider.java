package io.github.jiangood.openadmin.modules.job;

import io.github.jiangood.openadmin.modules.logviewer.perm.LogKeyPermissionProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 声明定时任务日志（{@code job/{logId}}）的查看权限。
 */
@Component
public class JobLogPermissionProvider implements LogKeyPermissionProvider {

    /**
     * 任务日志的 key 前缀，与 {@link BaseJob} 绑定的 key 保持一致
     */
    public static final String KEY_PREFIX = "job";

    @Override
    public Map<String, String> logKeyPermissions() {
        return Map.of(KEY_PREFIX, "job:read");
    }
}
