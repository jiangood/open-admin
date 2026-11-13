package io.github.jiangood.openadmin.modules.task.perm;

import io.github.jiangood.openadmin.modules.logviewer.perm.LogKeyPermissionProvider;
import io.github.jiangood.openadmin.modules.task.entity.SysTaskLog;
import io.github.jiangood.openadmin.modules.task.repository.SysTaskLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import static io.github.jiangood.openadmin.framework.MessageConst.MSG_FORBIDDEN;

/**
 * 任务日志（{@code task/{logId}}）动态权限：按任务记录的 permission 字段鉴权。
 * <p>
 * 与定时任务日志等静态前缀权限不同，每条任务记录可要求不同的查看权限，
 * 且必须存在对应记录才能查看（防止猜日志文件名）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskLogKeyPermissionProvider implements LogKeyPermissionProvider {

    /**
     * 任务日志 key 前缀，对应 logviewer key {@code task/{logId}}。
     */
    public static final String KEY_PREFIX = "task";

    private final SysTaskLogRepository taskLogRepository;

    @Override
    public String requiredPermission(String key) {
        if (key == null) {
            return null;
        }
        String prefix = KEY_PREFIX + "/";
        if (!key.startsWith(prefix)) {
            return null;
        }
        String logId = key.substring(prefix.length());
        if (logId.isBlank() || logId.contains("/")) {
            throw new AccessDeniedException(MSG_FORBIDDEN + "：非法的任务日志标识");
        }

        SysTaskLog task = taskLogRepository.findByLogId(logId).orElse(null);
        if (task == null) {
            log.warn("任务日志访问被拒绝，任务记录不存在: {}", logId);
            throw new AccessDeniedException(MSG_FORBIDDEN + "：任务记录不存在");
        }
        String permission = task.getPermission();
        if (permission == null || permission.isBlank()) {
            throw new AccessDeniedException(MSG_FORBIDDEN + "：该任务日志不可查看");
        }
        return permission;
    }
}
