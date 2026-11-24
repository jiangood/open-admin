package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 管理员密码状态提醒：只要 admin 的 {@code lastPasswordChangeTime} 为空（初始密码或管理员重置后从未修改），
 * 每次启动都打印一段醒目的英文 ASCII 横幅。
 * <p>
 * 只提醒、不输出密码本身，避免明文落库/落日志；需要新密码时执行 {@code --reset-admin-password}。
 * 顺序需晚于 {@link AdminPasswordResetRunner}（后者重置完会直接退出进程），避免同一轮里重复提示。
 * </p>
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AdminPasswordStatusReporter implements ApplicationRunner {

    private static final String ADMIN_ID = "1";

    private final SysUserService sysUserService;
    private final SeedDataInitializer seedDataInitializer;

    @Override
    public void run(ApplicationArguments args) {
        // 首启刚创建的账号已打印过含密码的横幅，本轮不再重复
        if (seedDataInitializer.isAdminCreatedThisRun()) {
            return;
        }

        SysUser admin = sysUserService.findById(ADMIN_ID)
                .or(() -> sysUserService.findByAccount("admin"))
                .orElse(null);

        if (admin == null || admin.getLastPasswordChangeTime() != null) {
            return;
        }

        log.warn(PasswordConsoleBanner.renderNotChangedBlock(
                admin.getAccount(), Boolean.TRUE.equals(admin.getEnabled())));
    }
}
