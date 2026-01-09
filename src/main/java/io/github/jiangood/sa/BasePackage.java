package io.github.jiangood.sa;

import io.github.jiangood.sa.common.tools.SpringTool;
import io.github.jiangood.sa.modules.BasePackageModules;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackageClasses = BasePackage.class)
@EntityScan(basePackageClasses = BasePackageModules.class)
@EnableCaching
@EnableScheduling
public class BasePackage {

    @Bean
    public SpringTool SpringTool() {
        System.out.println(SpringTool.class.getName() + " 初始化...");
        return new SpringTool();
    }
}
