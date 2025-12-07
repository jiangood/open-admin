package io.admin.modules.api.init;

import cn.hutool.core.util.StrUtil;
import io.admin.common.utils.SpringUtils;
import io.admin.modules.api.defaults.MathApi;
import io.admin.modules.api.service.ApiResourceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/***
 *
 * 开放接口
 * @gendoc
 * @see MathApi
 */
@Component
@Slf4j
@Order
public class ApiInit implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("解析接口...");
        init();
    }


    private void init() {
        Map<String, Object> beans = SpringUtils.getBeansOfType(Object.class);
        String[] basePackageNames = SpringUtils.getBasePackageNames();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();
            String pkg = bean.getClass().getPackageName();
            if (StrUtil.startWithAny(pkg, basePackageNames)) {
                service.saveOrUpdate(beanName, bean);
            }
        }
    }


    @Resource
    private ApiResourceService service;

}
