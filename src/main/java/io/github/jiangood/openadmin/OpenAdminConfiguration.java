package io.github.jiangood.openadmin;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = OpenAdminConfiguration.PKG)
@EntityScan(basePackages = OpenAdminConfiguration.PKG)
@EnableCaching
@EnableScheduling
public class OpenAdminConfiguration {

    public static final String PKG = "io.github.jiangood.openadmin";


}
