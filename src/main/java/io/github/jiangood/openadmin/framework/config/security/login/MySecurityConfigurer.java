package io.github.jiangood.openadmin.framework.config.security.login;

import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.lang.ResponseTool;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.modules.common.dto.LoginRequest;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.github.jiangood.openadmin.modules.common.AuthController.CAPTCHA_CODE;

public final class MySecurityConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, MySecurityConfigurer<H>, LoginFilter> {


    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, loginProcessingUrl);
    }


    @Override
    public void init(H http) throws Exception {
        this.setAuthenticationFilter(new LoginFilter());
        http.addFilterBefore(this.getAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        this.loginPage(LoginFilter.DEFAULT_LOGIN_URL);
        this.successHandler((request, response, authentication) -> {
            ResponseTool.responseJson(response, AjaxResult.ok("登录成功!" + MySecurityConfigurer.class.getName()));
        });


        this.getAuthenticationFilter().setAuthenticationConverter(request -> {
            HttpSession session = request.getSession();
            try {
                ServletInputStream is = request.getInputStream();
                String body = IOUtils.toString(is, StandardCharsets.UTF_8);
                LoginRequest loginRequest = JsonTool.jsonToBean(body, LoginRequest.class);

                String username = loginRequest.getUsername();
                String password = loginRequest.getPassword();
                String captchaCode = loginRequest.getCaptchaCode();
                String sessionCode = (String) session.getAttribute(CAPTCHA_CODE);

                Authentication token = new AuthToken(username,password,captchaCode,sessionCode);
                return token;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        super.init(http);
    }
}
