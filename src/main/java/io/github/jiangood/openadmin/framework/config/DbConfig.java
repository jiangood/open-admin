package io.github.jiangood.openadmin.framework.config;


import io.github.jiangood.openadmin.lang.jdbc.DbTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.sql.DataSource;

@Configuration
@EnableJpaAuditing
public class DbConfig {

    @Bean
    @ConditionalOnMissingBean(value = DbTool.class)
    public DbTool dbUtils(DataSource dataSource) {
        return new DbTool(dataSource);
    }
}
