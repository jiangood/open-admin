package io.github.jiangood.openadmin.framework.spi;

import io.github.jiangood.openadmin.framework.data.JdbcRunner;

public interface StartupHook {

    default void beforeJpaSchemaInitialize(JdbcRunner db) {
    }

    default void beforeSeedDataInitialize() {
    }

    default void afterSeedDataInitialize() {
    }
}
