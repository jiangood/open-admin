package io.github.jiangood.openadmin.framework.config.init;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

public interface OpenLifecycle {


    default void onDataInit() {

    }


    default void afterDataInit() {

    }



    default void onConfigSecurity(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception{

    }


    default void onConfigSecurityAuthorizeHttpRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {

    }

}
