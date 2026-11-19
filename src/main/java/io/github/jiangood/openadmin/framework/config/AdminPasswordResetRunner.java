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

        int[] exitCode = {0};
        try {
            // 框架种子数据保证管理员 id 为 "1"；优先按 id 查找，兼容账号被改名
            SysUser admin = sysUserService.findById(ADMIN_ID)
                    .orElseGet(() -> sysUserService.findByAccount("admin")
                            .orElseThrow(() -> new IllegalStateException(
                                    "未找到管理员账号（id=1 或 account=admin），无法重置密码")));

            if (!Boolean.TRUE.equals(admin.getEnabled())) {
                log.warn("管理员账号当前为禁用状态，密码已重置但仍无法登录，请同时在数据库中启用该账号");
            }

            // 不支持外部指定密码，仅生成随机强密码
            String newPassword = PasswordTool.random();
            sysUserService.resetPwd(admin.getId(), newPassword);

            // 审计：日志文件只记录发生了重置，不包含密码
            log.warn("[ADMIN-PASSWORD-RESET] 管理员密码已重置: account={} (新密码仅输出到控制台，请妥善保存并在登录后立即修改密码)",
                    admin.getAccount());
            // 密码只输出到控制台（stdout），不写入应用日志文件
            System.out.println("[ADMIN-PASSWORD-RESET] account=" + admin.getAccount()
                    + ", newPassword=" + newPassword
                    + " (请妥善保存并在登录后立即修改密码)");
        } catch (Exception e) {
            exitCode[0] = 1;
            log.error("管理员密码重置失败", e);
        } finally {
            try {
                int code = SpringApplication.exit(context, () -> exitCode[0]);
                System.exit(code);
            } catch (Throwable t) {
                // 兜底：确保进程退出，避免异常导致命令挂起
                Runtime.getRuntime().halt(exitCode[0] == 0 ? 1 : exitCode[0]);
            }
        }
    }
}
