package io.admin.framework.config.security;

import cn.hutool.core.util.StrUtil;
import io.admin.common.dto.AjaxResult;
import io.admin.common.utils.ResponseUtils;
import io.admin.framework.servlet.ReplaceParameterRequestWrapper;
import io.admin.modules.common.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 增加额外的登录逻辑，如 最大重试次数，验证码等
 * <p>
 * 前端密码解密
 */
@Slf4j
@AllArgsConstructor
@Component
public class LoginFilter extends OncePerRequestFilter {

    private final AuthService authService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        boolean isLoginUrl = "/admin/auth/login".equals(uri) && "POST".equals(method);
        if (!isLoginUrl) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = request.getParameter("username");
        try {
            authService.validate(request);
            String password = request.getParameter("password");

            String passwordType = (String) request.getAttribute("passwordType");
            passwordType = StrUtil.nullToDefault(passwordType, "WEB");
            if ("WEB".equals(passwordType)) {
                password = authService.decodeWebPassword(password);
            }

            ReplaceParameterRequestWrapper newRequest = new ReplaceParameterRequestWrapper(request);
            newRequest.replace("password", password);
            request = newRequest;
        } catch (Exception e) {
            log.error("用户[{}]认证失败： {}", username, e.getMessage());
            ResponseUtils.response(response, AjaxResult.err(e.getMessage()));
            return;
        }

        try {
            filterChain.doFilter(request, response);
            authService.onSuccess(username);
        } catch (Exception e) {
            authService.onFail(username);
            ResponseUtils.response(response, AjaxResult.err(e.getMessage()));
        }
    }

}
