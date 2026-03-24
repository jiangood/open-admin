package io.github.jiangood.openadmin.modules.api;

import cn.hutool.extra.spring.SpringUtil;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.utils.Constants;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class ApiConfigValidator implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {
        SpringDocConfigProperties springDocConfigProperties = SpringUtil.getBean(SpringDocConfigProperties.class);
        if (springDocConfigProperties != null) {
            String docPath = springDocConfigProperties.getApiDocs().getPath();
            Assert.state("/admin/api-docs".equals(docPath), "必须配置" + Constants.SPRINGDOC_PREFIX + ".path为/admin/api-docs");
        }

        SwaggerUiConfigProperties swaggerUiConfigProperties = SpringUtil.getBean(SwaggerUiConfigProperties.class);
        if (swaggerUiConfigProperties != null) {
            String swaggerPath = swaggerUiConfigProperties.getPath();
            Assert.state("/admin/".equals(swaggerPath), "必须配置" + Constants.SPRINGDOC_SWAGGER_PREFIX + ".path为/admin/");
        }
    }
}
