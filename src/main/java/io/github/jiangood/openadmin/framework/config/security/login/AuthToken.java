package io.github.jiangood.openadmin.framework.config.security.login;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class AuthToken extends AbstractAuthenticationToken {

    private final Object principal;
    private Object credentials;
    private  String sessionCode;
    private  String captchaCode;

    public AuthToken(Object principal, Object credentials, String captchaCode, String sessionCode) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        this.captchaCode = captchaCode;
        this.sessionCode = sessionCode;
    }
    public AuthToken(Object principal, Collection<? extends GrantedAuthority> auth) {
        super(auth);
        this.principal = principal;
        this.setAuthenticated(true);
    }

}
