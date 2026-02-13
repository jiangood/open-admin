package io.github.jiangood.openadmin;

import io.github.jiangood.openadmin.framework.data.BaseRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = OpenAdminConfiguration.PKG)
@EntityScan(basePackages = OpenAdminConfiguration.PKG)
@EnableCaching
@EnableScheduling
@EnableJpaRepositories(
        repositoryBaseClass = BaseRepositoryImpl.class
)
public class OpenAdminConfiguration {

    public static final String PKG =  "io.github.jiangood.openadmin";


}
