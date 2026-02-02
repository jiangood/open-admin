package io.github.jiangood.openadmin.framework.config.init;

import io.github.jiangood.openadmin.lang.SpringTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemHookService {

    private final Collection<SystemHook> interceptors;

    public void trigger(SystemHookEventType type) {
        for (SystemHook hook : interceptors) {
            hook.onEvent(type);
        }

        if (type == SystemHookEventType.BEFORE_DATA_INIT) {
            Collection<SystemHook> interceptors = SpringTool.getBeans(SystemHook.class);
            for (SystemHook it : interceptors) {
                log.warn("已弃用beforeDataInit: {}", it.getClass().getName());
                it.beforeDataInit();
            }
        }

        if (type == SystemHookEventType.AFTER_DATA_INIT) {
            Collection<SystemHook> interceptors = SpringTool.getBeans(SystemHook.class);
            for (SystemHook it : interceptors) {
                log.warn("已弃用afterDataInit: {}", it.getClass().getName());
                it.afterDataInit();
            }
        }
    }

    public void beforeConfigSecurity(HttpSecurity http) {
        for (SystemHook hook : interceptors) {
            hook.beforeConfigSecurity(http);
        }
    }
}
