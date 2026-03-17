package io.github.jiangood.openadmin.framework.config.init;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@AutoConfiguration(after = { DataSourceAutoConfiguration.class})
public class BeforeJpaConfiguration {
    

    @Bean
    public PreDdlDataSourceScriptDatabaseInitializer myData(DataSource ds, OpenLifecycleManager om) {
        return new PreDdlDataSourceScriptDatabaseInitializer(ds,om);
    }

    public static class PreDdlDataSourceScriptDatabaseInitializer extends DataSourceScriptDatabaseInitializer {
        private final OpenLifecycleManager om;
        public PreDdlDataSourceScriptDatabaseInitializer(DataSource dataSource, OpenLifecycleManager om) {
            super(dataSource, null);
            this.om = om;
        }

        @Override
        public boolean initializeDatabase() {
            om.beforeJpaInit();
            return true;
        }




    }
}