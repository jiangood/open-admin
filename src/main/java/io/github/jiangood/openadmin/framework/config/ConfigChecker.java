package io.github.jiangood.openadmin.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigChecker implements ApplicationRunner {

    @Value("${spring.config.import:}")
    private String configImport;

    @Override
    public void run(ApplicationArguments args) {
        if (!configImport.contains("application-lib.yml")) {
            log.error("""

                    未检测到 open-admin 框架配置！请在 application.yml 中添加:
                      spring:
                        config:
                          import: classpath:application-lib.yml
                    """);
            throw new IllegalStateException("框架配置缺失");
        }
    }
}
