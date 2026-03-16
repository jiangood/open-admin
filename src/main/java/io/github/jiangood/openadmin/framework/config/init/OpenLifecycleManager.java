package io.github.jiangood.openadmin.framework.config.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpenLifecycleManager {

    private final Collection<OpenLifecycle> lifecycles;



    public void onConfigSecurity(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        for (OpenLifecycle lifecycle : lifecycles) {
            lifecycle.onConfigSecurity(http,authenticationManager);
        }
    }


    public void onConfigSecurityAuthorizeHttpRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        for (OpenLifecycle lifecycle : lifecycles) {
            lifecycle.onConfigSecurityAuthorizeHttpRequests(authz);
        }
    }

    public void onDataInit() {
        for (OpenLifecycle lifecycle : lifecycles) {
            lifecycle.onDataInit();
        }
    }

    public void afterDataInit() {
        for (OpenLifecycle lifecycle : lifecycles) {
            lifecycle.afterDataInit();
        }
    }
}
