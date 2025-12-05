package io.admin.framework.config.init;

import io.admin.common.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
public class SystemHookService {

    public void trigger(SystemHookEventType type){
        for (SystemHook hook : SpringUtils.getBeans(SystemHook.class)) {
            hook.onEvent(type);
        }

        if(type == SystemHookEventType.BEFORE_DATA_INIT){
            Collection<SystemHook> interceptors = SpringUtils.getBeans(SystemHook.class);
            for (SystemHook it : interceptors) {
                log.warn("已弃用beforeDataInit: {}", it.getClass().getName());
                it.beforeDataInit();
            }
        }

        if(type == SystemHookEventType.AFTER_DATA_INIT){
            Collection<SystemHook> interceptors = SpringUtils.getBeans(SystemHook.class);
            for (SystemHook it : interceptors) {
                log.warn("已弃用afterDataInit: {}", it.getClass().getName());
                it.afterDataInit();
            }
        }
    }

    public void beforeConfigSecurity(HttpSecurity http) {
        for (SystemHook hook : SpringUtils.getBeans(SystemHook.class)) {
            hook.beforeConfigSecurity(http);
        }
    }
}
