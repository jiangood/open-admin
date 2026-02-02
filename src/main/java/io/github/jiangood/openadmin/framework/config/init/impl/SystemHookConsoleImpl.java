package io.github.jiangood.openadmin.framework.config.init.impl;

import io.github.jiangood.openadmin.framework.config.init.SystemHook;
import io.github.jiangood.openadmin.framework.config.init.SystemHookEventType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

@Component
public class SystemHookConsoleImpl implements SystemHook {

    @Override
    public void beforeDataInit() {
        System.out.println("SystemHookConsoleImpl: Executing beforeDataInit");
    }

    @Override
    public void afterDataInit() {
        System.out.println("SystemHookConsoleImpl: Executing afterDataInit");
    }

    @Override
    public void onEvent(SystemHookEventType eventType) {
        System.out.println("SystemHookConsoleImpl: Executing onEvent with eventType: " + eventType);
    }

    @Override
    public void beforeConfigSecurity(HttpSecurity http) {
        System.out.println("SystemHookConsoleImpl: Executing beforeConfigSecurity");
    }
}
