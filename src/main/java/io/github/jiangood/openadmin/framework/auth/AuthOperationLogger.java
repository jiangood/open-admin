package io.github.jiangood.openadmin.framework.auth;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import io.github.jiangood.openadmin.util.IpTool;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 登录、登出操作日志。
 *
 * <p>登录时请求尚未认证、登出时 SecurityContext 已被清空，{@code @Log} 切面拿不到操作者，
 * 因此这里手动构造 {@link SysLog}。不记录请求参数，避免密码进入日志。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthOperationLogger {

    static final String OPERATION_LOGIN = "认证-登录";
    static final String OPERATION_LOGIN_FAILED = "认证-登录失败";
    static final String OPERATION_LOGOUT = "认证-登出";

    /** 与 sys_log 的列长度保持一致 */
    private static final int OPERATION_MAX_LENGTH = 100;
    private static final int USERNAME_MAX_LENGTH = 50;
    private static final int DETAIL_MAX_LENGTH = 500;

    private final SysLogService sysLogService;

    public void loginSuccess(HttpServletRequest request, String account, LoginUser loginUser, long duration) {
        String name = loginUser != null && loginUser.getName() != null ? loginUser.getName() : account;
        SysLog sysLog = base(request, OPERATION_LOGIN, "账号「" + account + "」登录成功", name, duration);
        sysLog.setSuccess(true);
        if (loginUser != null) {
            sysLog.setUserId(loginUser.getId());
        }
        save(sysLog);
    }

    /**
     * 记录失败登录：密码错误、账号锁定、会话超限、密码解析失败等都走这里。
     */
    public void loginFailed(HttpServletRequest request, String account, String reason, long duration) {
        String message = reason == null || reason.isBlank() ? "登录失败" : reason;
        SysLog sysLog = base(request, OPERATION_LOGIN_FAILED,
                "账号「" + account + "」登录失败：" + message, account, duration);
        sysLog.setSuccess(false);
        sysLog.setError(message);
        save(sysLog);
    }

    public void logout(HttpServletRequest request, LoginUser loginUser, long duration) {
        if (loginUser == null) {
            // 未登录状态下调用登出（如会话已失效），没有可记录的操作者
            return;
        }
        SysLog sysLog = base(request, OPERATION_LOGOUT,
                "账号「" + loginUser.getUsername() + "」退出登录", loginUser.getName(), duration);
        sysLog.setSuccess(true);
        sysLog.setUserId(loginUser.getId());
        save(sysLog);
    }

    private SysLog base(HttpServletRequest request, String operation, String detail,
                        String username, long duration) {
        SysLog sysLog = new SysLog();
        sysLog.setOperation(truncate(operation, OPERATION_MAX_LENGTH));
        sysLog.setDetail(truncate(detail, DETAIL_MAX_LENGTH));
        sysLog.setUsername(truncate(username, USERNAME_MAX_LENGTH));
        sysLog.setIp(IpTool.getIp(request));
        sysLog.setOperationTime(LocalDateTime.now(ZoneId.systemDefault()));
        sysLog.setDuration((int) duration);
        return sysLog;
    }

    private void save(SysLog sysLog) {
        try {
            sysLogService.saveOperationLogAsync(sysLog);
        } catch (Exception e) {
            // 日志失败不能影响登录/登出本身
            log.warn("保存认证操作日志失败: {}", e.getMessage());
        }
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
