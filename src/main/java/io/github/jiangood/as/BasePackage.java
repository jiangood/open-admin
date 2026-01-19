package io.github.jiangood.as;

import io.github.jiangood.as.common.tools.SpringTool;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = {"io.github.jiangood.as.framework", "io.github.jiangood.as.modules"})
@EntityScan(basePackages = "io.github.jiangood.as.modules")
@EnableCaching
@EnableScheduling
public class BasePackage {

    @Bean
    public SpringTool SpringTool() {
        return new SpringTool();
    }
}
