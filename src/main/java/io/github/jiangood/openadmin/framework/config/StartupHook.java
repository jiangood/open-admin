package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.framework.data.JdbcRunner;

/**
 * 系统启动过程的钩子接口，供业务项目实现以介入 open-admin 初始化流程。
 *
 * <p>按执行顺序：
 * <ol>
 *   <li>{@link #beforeJpaSchemaInitialize(JdbcRunner)} — JPA 自动建表之前</li>
 *   <li>{@link #beforeSeedDataInitialize()} — Flyway 种子数据迁移之前</li>
 *   <li>{@link #afterSeedDataInitialize()} — Flyway 种子数据迁移之后</li>
 * </ol>
 *
 * @see DbConfig
 */
public interface StartupHook {

    /**
     * JPA 自动建表之前执行，可使用 {@link JdbcRunner} 执行原生 SQL。
     * 例如创建视图、在 JPA 自动建表之前调整表结构等。
     */
    default void beforeJpaSchemaInitialize(JdbcRunner db) {
    }

    /** Flyway 种子数据迁移之前执行 */
    default void beforeSeedDataInitialize() {
    }

    /** Flyway 种子数据迁移之后执行 */
    default void afterSeedDataInitialize() {
    }
}
