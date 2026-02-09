package io.github.jiangood.openadmin.framework.config.security.login;

import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.modules.common.dto.LoginRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.github.jiangood.openadmin.modules.common.AuthController.CAPTCHA_CODE;

public class LoginFilter extends AbstractAuthenticationProcessingFilter {
    public static final String DEFAULT_LOGIN_URL = "/admin/auth/login";
    public LoginFilter() {
        super(DEFAULT_LOGIN_URL);
        this.setAuthenticationConverter();
    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        Authentication authentication = parseToken(request);

        if (authentication == null) {
            return null;
        }
        Authentication result = this.getAuthenticationManager().authenticate(authentication);
        if (result == null) {
            throw new ServletException("AuthenticationManager should not return null Authentication object.");
        }
        return result;


    }

    @NotNull
    private static Authentication parseToken(HttpServletRequest request) {

    }


}
