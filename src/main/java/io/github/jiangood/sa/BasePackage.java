package io.github.jiangood.sa;

import io.github.jiangood.sa.common.tools.SpringTool;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = {"io.github.jiangood.sa.framework", "io.github.jiangood.sa.modules"})
@EntityScan(basePackages = "io.github.jiangood.sa.modules")
@EnableCaching
@EnableScheduling
public class BasePackage {

    @Bean
    public SpringTool SpringTool() {
        return new SpringTool();
    }
}
