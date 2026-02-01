package io.github.jiangood.openadmin;

import io.github.jiangood.openadmin.lang.SpringTool;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = {"io.github.jiangood.openadmin.framework", "io.github.jiangood.openadmin.modules"})
@EntityScan(basePackages = "io.github.jiangood.openadmin.modules")
@EnableCaching
@EnableScheduling
public class BasePackage {

    @Bean
    public SpringTool SpringTool() {
        return new SpringTool();
    }
}
