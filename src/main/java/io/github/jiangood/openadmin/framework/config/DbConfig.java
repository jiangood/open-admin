package io.github.jiangood.openadmin.framework.config;


import io.github.jiangood.openadmin.framework.data.JdbcRunner;
import io.github.jiangood.openadmin.framework.data.impl.JdbcRunnerImpl;
import io.github.jiangood.openadmin.framework.spi.StartupHook;
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
    @ConditionalOnMissingBean(value = JdbcRunner.class)
    public JdbcRunner jdbcRunner(DataSource dataSource) {
        return new JdbcRunnerImpl(dataSource);
    }


    @Bean
    public PreDdlDataSourceScriptDatabaseInitializer myData(DataSource ds, JdbcRunner db, List<StartupHook> startupHooks) {
        return new PreDdlDataSourceScriptDatabaseInitializer(ds, db, startupHooks);
    }

    /**
     * 在 JPA 建表后初始化种子数据：先执行前置钩子，再幂等写入框架种子数据，最后执行后置钩子
     * （如 {@code DictSeedSync} 的枚举字典同步）。
     */
    @Bean
    @Order(-1)
    CommandLineRunner seedDataRunner(SeedDataInitializer seedDataInitializer, List<StartupHook> startupHooks) {
        return args -> {
            startupHooks.forEach(StartupHook::beforeSeedDataInitialize);
            seedDataInitializer.initialize();
            startupHooks.forEach(StartupHook::afterSeedDataInitialize);
        };
    }

    /**
     * 在 JPA 自动建表之前执行 {@link StartupHook#beforeJpaSchemaInitialize(JdbcRunner)} 钩子。
     * 不执行任何 SQL 脚本（{@code super(dataSource, null)}），仅用于生命周期回调。
     */
    public static class PreDdlDataSourceScriptDatabaseInitializer extends DataSourceScriptDatabaseInitializer {
        private final List<StartupHook> startupHooks;
        private final JdbcRunner db;
        public PreDdlDataSourceScriptDatabaseInitializer(DataSource dataSource, JdbcRunner db, List<StartupHook> startupHooks) {
            super(dataSource, null); // NOSONAR: 有意不执行 SQL 脚本（见类注释），Spring 构造器参数允许 null
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
