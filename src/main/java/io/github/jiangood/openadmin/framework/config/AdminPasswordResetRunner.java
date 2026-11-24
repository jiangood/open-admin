package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import io.github.jiangood.openadmin.util.PasswordTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 命令行一次性重置管理员密码：传入 {@code --reset-admin-password} 时执行，完成后自动退出进程。
 * <p>
 * 不接受指定密码，仅生成随机强密码；新密码仅输出到控制台（stdout），不写入应用日志文件。
 */
@Slf4j
@Component
@Order(0) // seedDataRunner 为 @Order(-1)，需在其之后执行以确保 admin 已初始化
@RequiredArgsConstructor
public class AdminPasswordResetRunner implements ApplicationRunner {

    private static final String ADMIN_ID = "1";

    private final SysUserService sysUserService;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        // 仅支持命令行参数：--reset-admin-password
        if (!args.containsOption("reset-admin-password")) {
            return;
        }

        int exitCode = 0;
        String account = null;
        String newPassword = null;
        String failureReason = null;
        try {
            // 框架种子数据保证管理员 id 为 "1"；优先按 id 查找，兼容账号被改名
            SysUser admin = sysUserService.findById(ADMIN_ID)
                    .orElseGet(() -> sysUserService.findByAccount("admin")
                            .orElseThrow(() -> new IllegalStateException(
                                    "ADMIN ACCOUNT NOT FOUND (id=1 or account=admin)")));

            account = admin.getAccount();

            if (!Boolean.TRUE.equals(admin.getEnabled())) {
                log.warn("[ADMIN-PASSWORD-RESET] ADMIN ACCOUNT IS DISABLED. "
                        + "The password will still be reset, but login remains blocked until the account is enabled.");
            }

            // 不支持外部指定密码，仅生成随机强密码（GEN- 前缀，便于识别系统生成密码）
            newPassword = PasswordTool.random();
            sysUserService.resetPwd(admin.getId(), newPassword);

            // 审计：日志文件只记录发生了重置，不包含密码
            log.warn("[ADMIN-PASSWORD-RESET] Admin password reset: account={}. "
                    + "The new password is printed to the console only and is not written to the log file.", account);
        } catch (Exception e) {
            exitCode = 1;
            log.error("[ADMIN-PASSWORD-RESET] FAILED", e);
            failureReason = PasswordConsoleBanner.asciiOrFallback(e.getMessage());
        } finally {
            final int errorCode = exitCode;
            int code;
            try {
                // 先关闭 Spring，再打印横幅：确保密码是进程退出前最后一段输出，不被关闭日志淹没
                code = SpringApplication.exit(context, () -> errorCode);
            } catch (Throwable t) {
                code = errorCode == 0 ? 1 : errorCode;
                log.error("[ADMIN-PASSWORD-RESET] FAILED TO CLOSE APPLICATION", t);
            }

            // 密码只输出到控制台（stdout），不写入应用日志文件
            if (newPassword != null) {
                PasswordConsoleBanner.printToStdout(PasswordConsoleBanner.renderPasswordBlock(
                        "[ADMIN-PASSWORD-RESET]", "SUCCESS", account, newPassword));
            } else {
                PasswordConsoleBanner.printToStdout(PasswordConsoleBanner.renderFailureBlock(
                        "[ADMIN-PASSWORD-RESET]",
                        failureReason == null ? "UNKNOWN ERROR" : failureReason));
            }

            try {
                System.exit(code);
            } catch (Throwable t) {
                // 兜底：确保进程退出，避免异常导致命令挂起
                Runtime.getRuntime().halt(code == 0 ? 1 : code);
            }
        }
    }
}
