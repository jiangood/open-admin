package io.github.jiangood.openadmin.framework.config.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class FrameworkLifecycleManager {

    private final Collection<FrameworkLifecycle> lifecycles;



    public void onConfigSecurity(HttpSecurity http) {
        for (FrameworkLifecycle lifecycle : lifecycles) {
            lifecycle.onConfigSecurity(http);
        }
    }


    public void onConfigSecurityAuthorizeHttpRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        for (FrameworkLifecycle lifecycle : lifecycles) {
            lifecycle.onConfigSecurityAuthorizeHttpRequests(authz);
        }
    }

    public void onDataInit() {
        for (FrameworkLifecycle lifecycle : lifecycles) {
            lifecycle.onDataInit();
        }
    }

    public void afterDataInit() {
        for (FrameworkLifecycle lifecycle : lifecycles) {
            lifecycle.afterDataInit();
        }
    }
}
