package io.github.jiangood.openadmin.framework.config.init;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * 请使用
 */

public interface FrameworkLifecycle {


    default void onDataInit() {

    }


    default void afterDataInit() {

    }



    default void onConfigSecurity(HttpSecurity http) {

    }


    default void onConfigSecurityAuthorizeHttpRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {

    }

}
