package io.github.jiangood.openadmin.framework.config.security.login;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
public class DefaultAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String sessionCode;
    private final String captchaCode;

    public DefaultAuthenticationToken(Object principal, Object credentials, String captchaCode, String sessionCode) {
        super(principal, credentials);
        this.captchaCode = captchaCode;
        this.sessionCode = sessionCode;
    }


}
