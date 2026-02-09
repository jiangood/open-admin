package io.github.jiangood.openadmin.framework.config.security.login;

import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.modules.common.dto.LoginRequest;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.github.jiangood.openadmin.modules.common.AuthController.CAPTCHA_CODE;

public class AuthTokenConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        HttpSession session = request.getSession();
        try {
            ServletInputStream is = request.getInputStream();
            String body = IOUtils.toString(is, StandardCharsets.UTF_8);
            LoginRequest loginRequest = JsonTool.jsonToBean(body, LoginRequest.class);

            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            String captchaCode = loginRequest.getCaptchaCode();
            String sessionCode = (String) session.getAttribute(CAPTCHA_CODE);

            Authentication token = new AuthToken(username, password, captchaCode, sessionCode);
            return token;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}