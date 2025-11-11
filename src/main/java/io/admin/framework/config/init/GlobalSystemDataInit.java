package io.admin.framework.config.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.RSA;
import io.admin.Build;
import io.admin.common.utils.PasswordUtils;
import io.admin.framework.config.SysProp;
import io.admin.framework.dict.DictAnnHandler;
import io.admin.framework.dict.DictFieldAnnHandler;
import io.admin.modules.system.ConfigTool;
import io.admin.modules.system.dao.SysConfigDao;
import io.admin.modules.system.dao.SysUserDao;
import io.admin.modules.system.entity.DataPermType;
import io.admin.modules.system.entity.SysConfig;
import io.admin.modules.system.entity.SysRole;
import io.admin.modules.system.entity.SysUser;
import io.admin.modules.system.service.SysRoleService;
import io.admin.framework.db.DbCacheDao;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 系统数据初始化
 */
@Slf4j
@Component(GlobalSystemDataInit.BEAN_NAME)
@Order(0)
public class GlobalSystemDataInit implements CommandLineRunner {


    public static final String BEAN_NAME = "sysInit";

    public static final String CACHE_KEY_FRAMEWORK_VERSION = "FRAMEWORK_VERSION";

    @Resource
    SysRoleService sysRoleService;

    @Resource
    SysUserDao sysUserDao;


    @Resource
    SysConfigDao sysConfigDao;






    @Resource
    DictAnnHandler dictAnnHandler;


    @Resource
    DictFieldAnnHandler dictFieldAnnHandler;





    @Resource
    private DbCacheDao dbCacheDao;

    @Resource
    SysProp sysProp;

    @Value("${spring.application.name}")
    String applicationName;


    @Resource
    private SystemHookService systemHookService;
    @Override
    public void run(String... args) throws Exception {
        systemHookService.trigger(SystemHookEventType.BEFORE_DATA_INIT);






        log.info("执行初始化程序： {}", getClass().getName());
        long time = System.currentTimeMillis();

        dictAnnHandler.run();
        dictFieldAnnHandler.run();
        systemHookService.trigger(SystemHookEventType.AFTER_SYSTEM_MENU_INIT);

        SysRole adminRole = sysRoleService.initDefaultAdmin();
        initUser(adminRole);

        initSysConfigDefaultValue();

        log.info("系统初始化耗时：{}", System.currentTimeMillis() - time);

        systemHookService.trigger(SystemHookEventType.AFTER_DATA_INIT);
    }

    private void initSysConfigDefaultValue() {
        log.info("初始化系统配置的默认值");
        SysConfig pub = sysConfigDao.findOne(ConfigTool.RSA_PUBLIC_KEY);
        Assert.notNull(pub,"初始化脚本config.sql未执行");
        if (pub.getValue() == null && pub.getDefaultValue() == null) {
            log.info("随机生成RSA的公私钥");
            RSA rsa = SecureUtil.rsa();
            sysConfigDao.setDefaultValue(ConfigTool.RSA_PUBLIC_KEY, rsa.getPublicKeyBase64()); // 放到siteInfo, 前端可获取
            sysConfigDao.setDefaultValue( ConfigTool.RSA_PRIVATE_KEY, rsa.getPrivateKeyBase64());
        }

        sysConfigDao.setDefaultValue("sys.default.password", PasswordUtils.random());
        sysConfigDao.cleanCache();
    }



    private void initUser(SysRole adminRole) {
        log.info("-------------------------------------------");
        log.info("初始化管理员中....");
        String id = "admin";
        SysUser admin = sysUserDao.findOne(id);
        String account = "admin-" + applicationName;


        if (admin == null) {
            String pwd = PasswordUtils.random();
            admin = new SysUser();
            admin.setId(id);
            admin.setAccount(account);
            admin.setName("管理员");
            admin.setEnabled(true);
            admin.getRoles().add(adminRole);
            admin.setDataPermType(DataPermType.ALL);
            admin.setPassword(PasswordUtils.encode(pwd));
            admin = sysUserDao.save(admin);
            log.info("创建默认管理员 {}", admin.getAccount());
            dbCacheDao.set("admin_default_pwd", pwd);
            log.info("默认密码为： {}", pwd);
        }
        log.info("管理员登录账号:{}", admin.getAccount());

        String pwd = sysProp.getResetAdminPwd();
        if(StrUtil.isNotEmpty(pwd)){
            admin.setPassword(PasswordUtils.encode(pwd));
            log.info("管理员密码重置为 {}", pwd);
            sysUserDao.save(admin);
        }

        log.info("-------------------------------------------");
    }


}
