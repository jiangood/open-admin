package io.github.jiangood.as.modules.system;

import cn.hutool.extra.spring.SpringUtil;
import io.github.jiangood.as.modules.system.service.SysConfigService;

/**
 * 表sys_config 的一些便捷函数，常量 （并未包含所有）
 */
public class ConfigConsts {

    public static final String RSA_PUBLIC_KEY = "sys.rsa.publicKey";
    public static final String RSA_PRIVATE_KEY = "sys.rsa.privateKey";


    private static final SysConfigService service = SpringUtil.getBean(SysConfigService.class);

    public static String get(String key) {
        return service.get(key);
    }

}
