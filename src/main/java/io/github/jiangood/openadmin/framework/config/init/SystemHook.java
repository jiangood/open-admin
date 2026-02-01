package io.github.jiangood.openadmin.framework.config.init;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface SystemHook {


    default void beforeDataInit() {

    }


    default void afterDataInit() {

    }


    default void onEvent(SystemHookEventType eventType) {

    }

    default void beforeConfigSecurity(HttpSecurity http) {

    }
}
