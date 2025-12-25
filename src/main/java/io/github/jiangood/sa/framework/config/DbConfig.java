package io.github.jiangood.sa.framework.config;


import io.github.jiangood.sa.common.tools.db.DbTool;
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
