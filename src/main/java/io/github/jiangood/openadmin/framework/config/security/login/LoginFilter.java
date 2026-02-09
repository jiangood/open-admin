package io.github.jiangood.openadmin.framework.config.security.login;

import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    public LoginFilter(String url) {
        super(url);
    }

}
