package io.github.jiangood.openadmin.framework.config.init;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.lang.PasswordTool;
import io.github.jiangood.openadmin.framework.config.SysProperties;
import io.github.jiangood.openadmin.modules.system.dao.SysUserDao;
import io.github.jiangood.openadmin.modules.system.entity.DataPermType;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 系统数据初始化
 */
@Slf4j
@Component(GlobalSystemDataInit.BEAN_NAME)
@Order(0)
public class GlobalSystemDataInit implements CommandLineRunner {


    public static final String BEAN_NAME = "sysInit";

    @Resource
    SysRoleService sysRoleService;

    @Resource
    SysUserDao sysUserDao;




    @Resource
    SysProperties sysProperties;

    @Value("${spring.application.name}")
    String applicationName;


    @Resource
    private SystemHookService systemHookService;

    @Override
    public void run(String... args) throws Exception {
        systemHookService.trigger(SystemHookEventType.BEFORE_DATA_INIT);


        log.info("执行初始化程序： {}", getClass().getName());
        long time = System.currentTimeMillis();

        systemHookService.trigger(SystemHookEventType.AFTER_SYSTEM_MENU_INIT);

        SysRole adminRole = sysRoleService.initDefaultAdmin();
        initUser(adminRole);


        log.info("系统初始化耗时：{}", System.currentTimeMillis() - time);

        systemHookService.trigger(SystemHookEventType.AFTER_DATA_INIT);
    }


    private void initUser(SysRole adminRole) {
        log.info("-------------------------------------------");
        log.info("初始化管理员中....");
        String account = "admin-" + applicationName;

        SysUser admin = sysUserDao.findByAccount(account);
        if (admin == null) {
            String pwd = PasswordTool.random();
            admin = new SysUser();
            admin.setAccount(account);
            admin.setName("管理员");
            admin.setEnabled(true);
            admin.getRoles().add(adminRole);
            admin.setDataPermType(DataPermType.ALL);
            admin.setPassword(PasswordTool.encode(pwd));
            admin = sysUserDao.save(admin);
            log.info("创建默认管理员 {}", admin.getAccount());
        }
        log.info("管理员登录账号:{}", admin.getAccount());

        String pwd = sysProperties.getResetAdminPwd();
        if (StrUtil.isNotEmpty(pwd)) {
            admin.setPassword(PasswordTool.encode(pwd));
            log.info("管理员密码重置为 {}", pwd);
            sysUserDao.save(admin);
        }

        log.info("-------------------------------------------");
    }


}
