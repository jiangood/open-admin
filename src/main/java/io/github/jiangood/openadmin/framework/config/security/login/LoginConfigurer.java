package io.github.jiangood.openadmin.framework.config.security.login;

import io.github.jiangood.openadmin.lang.ResponseTool;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Slf4j
public final class LoginConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, LoginConfigurer<H>, LoginFilter> {

    public static final String DEFAULT_LOGIN_URL = "/admin/auth/login";

    @Override
    public void init(H http) throws Exception {
        // 设置过滤器
        LoginFilter authFilter = new LoginFilter(DEFAULT_LOGIN_URL);
        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        this.setAuthenticationFilter(authFilter);

        // 登录页
        this.loginPage(DEFAULT_LOGIN_URL);

        // 解析参数
        authFilter.setAuthenticationConverter(new AuthTokenConverter());

        // 响应json
        this.successHandler(LoginConfigurer::extracted);
        this.failureHandler(LoginConfigurer::failureHandler);


        super.init(http);
    }

    private static void extracted(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        ResponseTool.responseJson(response, AjaxResult.ok("登录成功，欢迎您" + authentication.getName() + "!"));
    }

    private static void failureHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        String message = "账号或密码错误";
        if (exception instanceof SessionAuthenticationException) {
            message = "您的账号已在其他地方登录，本次登录被拒绝";
        }

        log.info("登录失败 {} {}", exception.getClass().getName(), exception.getMessage());


        ResponseTool.responseJson(response, AjaxResult.err(message));
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, loginProcessingUrl);
    }


}
