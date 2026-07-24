package io.github.jiangood.openadmin.framework.config;


import io.github.jiangood.openadmin.util.jdbc.DbTool;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
    public PreDdlDataSourceScriptDatabaseInitializer myData(DataSource ds, DbTool db, List<StartupHook> startupHooks) {
        return new PreDdlDataSourceScriptDatabaseInitializer(ds, db, startupHooks);
    }

    @Bean
    @Order(-1)
    CommandLineRunner flywayRunner(DataSource dataSource, List<StartupHook> startupHooks) {
        return args -> {
            startupHooks.forEach(StartupHook::beforeSeedDataInitialize);

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations( "classpath:db/migration")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .ignoreMigrationPatterns(ValidatePattern.fromPattern("*:*"))
                    .load();
            flyway.migrate();

            startupHooks.forEach(StartupHook::afterSeedDataInitialize);
        };
    }

    /**
     * 在 JPA 自动建表之前执行 {@link StartupHook#beforeJpaSchemaInitialize(DbTool)} 钩子。
     * 不执行任何 SQL 脚本（{@code super(dataSource, null)}），仅用于生命周期回调。
     */
    public static class PreDdlDataSourceScriptDatabaseInitializer extends DataSourceScriptDatabaseInitializer {
        private final List<StartupHook> startupHooks;
        private final DbTool db;
        public PreDdlDataSourceScriptDatabaseInitializer(DataSource dataSource, DbTool db, List<StartupHook> startupHooks) {
            super(dataSource, null);
            this.startupHooks = startupHooks;
            this.db = db;
        }

        @Override
        public boolean initializeDatabase() {
            for (StartupHook hook : startupHooks) {
                hook.beforeJpaSchemaInitialize(db);
            }
            return true;
        }
    }
}
