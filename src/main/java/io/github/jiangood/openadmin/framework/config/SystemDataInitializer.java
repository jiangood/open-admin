package io.github.jiangood.openadmin.framework.config;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.util.PasswordTool;
import io.github.jiangood.openadmin.util.jdbc.DbTool;
import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.repository.SysUserRepository;
import io.github.jiangood.openadmin.modules.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 系统数据初始化
 */
@Slf4j
@Component(SystemDataInitializer.BEAN_NAME)
@Order(0)
@RequiredArgsConstructor
public class SystemDataInitializer implements CommandLineRunner {

    public static final String BEAN_NAME = "sys_init";

    private final SysRoleService sysRoleService;
    private final SysUserRepository sysUserRepository;
    private final SystemProperties systemProperties;
    private final DbTool dbTool;
    private final List<OpenLifecycle> lifecycles;

    @Override
    public void run(String... args) throws Exception {
        lifecycles.forEach(OpenLifecycle::onDataInit);

        log.info("执行初始化程序： {}", getClass().getName());
        long time = System.currentTimeMillis();

        initDict();
        SysRole adminRole = sysRoleService.initDefaultAdmin();
        initUser(adminRole);

        lifecycles.forEach(OpenLifecycle::afterDataInit);

        log.info("系统初始化耗时：{}", System.currentTimeMillis() - time);
    }

    private void initDict() throws Exception {
        Integer count = dbTool.findInteger("SELECT COUNT(*) FROM sys_dict_item");
        if (count != null && count > 0) {
            log.info("字典数据已存在，跳过初始化");
            return;
        }
        log.info("初始化字典数据...");

        try {
            dbTool.execute("ALTER TABLE sys_dict_type DROP COLUMN builtin");
        } catch (Exception e) {
            log.debug("builtin 列不存在或已删除: {}", e.getMessage());
        }

        for (String sqlResource : new String[]{"data/dict-type-init.sql", "data/dict-init.sql"}) {
            ClassPathResource resource = new ClassPathResource(sqlResource);
            String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            for (String stmt : sql.split(";")) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty()) {
                    dbTool.execute(trimmed);
                }
            }
        }
        log.info("字典数据初始化完成");
    }

    private void initUser(SysRole adminRole) {
        log.info("-------------------------------------------");
        log.info("初始化管理员中....");
        String account = "admin";

        SysUser admin = sysUserRepository.findByAccount(account).orElse(null);
        if (admin == null) {
            String pwd = systemProperties.getDefaultPassword();
            if (StrUtil.isEmpty(pwd)) {
                throw new IllegalStateException("请在配置文件中设置 sys.default-password");
            }
            admin = new SysUser();
            admin.setAccount(account);
            admin.setName("管理员");
            admin.setEnabled(true);
            admin.getRoles().add(adminRole);
            admin.setDataPermType(DataPermType.ALL);
            admin.setPassword(PasswordTool.encode(pwd));
            admin = sysUserRepository.save(admin);
            log.info("创建默认管理员 {}, 密码: {}", admin.getAccount(), pwd);
        }
        log.info("管理员登录账号:{}", admin.getAccount());

        String pwd = systemProperties.getResetAdminPwd();
        if (StrUtil.isNotEmpty(pwd)) {
            admin.setPassword(PasswordTool.encode(pwd));
            log.info("管理员密码重置为 {}", pwd);
            sysUserRepository.save(admin);
        }

        log.info("-------------------------------------------");
    }
}
