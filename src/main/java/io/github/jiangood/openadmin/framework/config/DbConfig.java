package io.github.jiangood.openadmin.framework.config;


import io.github.jiangood.openadmin.util.jdbc.DbTool;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableJpaAuditing
@AutoConfiguration(after = { DataSourceAutoConfiguration.class})
public class DbConfig {

    @Bean
    @ConditionalOnMissingBean(value = DbTool.class)
    public DbTool dbTool(DataSource dataSource) {
        return new DbTool(dataSource);
    }


    @Bean
    public PreDdlDataSourceScriptDatabaseInitializer myData(DataSource ds, DbTool db, List<OpenLifecycleBeforeJpaInit> beforeJpaInitList) {
        return new PreDdlDataSourceScriptDatabaseInitializer(ds,db,beforeJpaInitList);
    }

    public static class PreDdlDataSourceScriptDatabaseInitializer extends DataSourceScriptDatabaseInitializer {
        private final List<OpenLifecycleBeforeJpaInit> lifecycleBeforeJpaInitList;
        private final DbTool db;
        public PreDdlDataSourceScriptDatabaseInitializer(DataSource dataSource, DbTool db, List<OpenLifecycleBeforeJpaInit> lifecycleBeforeJpaInitList) {
            super(dataSource, null);
            this.lifecycleBeforeJpaInitList = lifecycleBeforeJpaInitList;
            this.db = db;
        }

        @Override
        public boolean initializeDatabase() {
            for (OpenLifecycleBeforeJpaInit lf : lifecycleBeforeJpaInitList) {
                lf.process(db);
            }
            return true;
        }
    }
}
