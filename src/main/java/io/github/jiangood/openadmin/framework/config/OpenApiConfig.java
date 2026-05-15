package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.modules.api.filter.ApiConstant;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private final String AUTH_HEADER_NAME = "BearerAuth";

    @Bean
    public GroupedOpenApi openApi() {
        return GroupedOpenApi.builder()
                .group("open-api") // 分组名称
                .pathsToMatch(ApiConstant.BASE_URL +"/**") // 只有匹配此路径的接口才应用
                .addOpenApiCustomizer(openApi -> {
                    // 1. 定义安全方案（Header名、类型等）
                    openApi.getComponents()
                            .addSecuritySchemes(AUTH_HEADER_NAME,
                                    new SecurityScheme()
                                            .scheme("bearer")
                                            .description("请输入您的有效 Token (无需手动输入 Bearer 前缀)")
                            );

                    // 2. 将该方案应用到当前分组
                    openApi.addSecurityItem(new SecurityRequirement().addList(AUTH_HEADER_NAME));
                })
                .build();
    }


}