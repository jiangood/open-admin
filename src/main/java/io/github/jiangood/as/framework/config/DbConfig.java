package io.github.jiangood.as.framework.config;


import io.github.jiangood.as.common.tools.jdbc.DbTool;
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
