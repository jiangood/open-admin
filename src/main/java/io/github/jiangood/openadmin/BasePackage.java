package io.github.jiangood.openadmin;

import io.github.jiangood.openadmin.lang.SpringTool;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = BasePackage.PKG)
@EntityScan(basePackages = BasePackage.PKG)
@EnableCaching
@EnableScheduling
public class BasePackage {

    public static final String PKG = "io.github.jiangood.openadmin";

    @Bean
    public SpringTool SpringTool() {
        return new SpringTool();
    }
}
