package io.github.jiangood.openadmin.modules.api.init;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.common.tools.SpringTool;
import io.github.jiangood.openadmin.modules.api.defaults.MathApi;
import io.github.jiangood.openadmin.modules.api.service.ApiResourceService;
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

    @Resource
    private ApiResourceService service;

    @Override
    public void run(String... args) throws Exception {
        log.info("解析接口...");
        init();
    }

    private void init() {
        Map<String, Object> beans = SpringTool.getBeansOfType(Object.class);
        String[] basePackageNames = SpringTool.getBasePackageNames();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            String beanName = entry.getKey();
            Object bean = entry.getValue();
            String pkg = bean.getClass().getPackageName();
            if (StrUtil.startWithAny(pkg, basePackageNames)) {
                service.saveOrUpdate(beanName, bean);
            }
        }
    }

}
