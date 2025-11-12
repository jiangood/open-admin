package io.admin.framework.data.config;


import io.admin.common.utils.DbUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbUtilsConfig {

    @Bean
    @ConditionalOnMissingBean(value = DbUtils.class)
    public DbUtils dbUtils(DataSource dataSource){
        return new DbUtils(dataSource);
    }
}
