package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.util.jdbc.DbTool;

/**
 * 系统启动过程的钩子接口，供业务项目实现以介入 open-admin 初始化流程。
 *
 * <p>按执行顺序：
 * <ol>
 *   <li>{@link #beforeJpaSchemaInitialize(DbTool)} — JPA 自动建表之前</li>
 *   <li>{@link #beforeSystemDataInitialize()} — 系统数据初始化之前</li>
 *   <li>{@link #afterSystemDataInitialize()} — 系统数据初始化之后</li>
 * </ol>
 *
 * @see SystemDataInitializer
 * @see DbConfig
 */
public interface StartupHook {

    /**
     * JPA 自动建表之前执行，可使用 {@link DbTool} 执行原生 SQL。
     * 例如创建视图、在 JPA 自动建表之前调整表结构等。
     */
    default void beforeJpaSchemaInitialize(DbTool db) {
    }

    /** 系统数据初始化（字典、默认角色等）之前执行 */
    default void beforeSystemDataInitialize() {
    }

    /** 系统数据初始化之后执行 */
    default void afterSystemDataInitialize() {
    }
}
