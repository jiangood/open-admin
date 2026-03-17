package io.github.jiangood.openadmin.framework.config.init;

import jakarta.persistence.PrePersist;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

public interface OpenLifecycle {

    @PrePersist
    default void beforeJpaInit(){

    }


    default void onDataInit() {

    }


    default void afterDataInit() {

    }



    default void onConfigSecurity(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception{

    }


    default void onConfigSecurityAuthorizeHttpRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {

    }

}
