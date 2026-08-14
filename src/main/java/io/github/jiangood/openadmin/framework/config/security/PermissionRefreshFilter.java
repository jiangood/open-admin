package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.ResponseTool;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 检查授权数据是否需要刷新
 */
@Component
@AllArgsConstructor
public class PermissionRefreshFilter extends OncePerRequestFilter {

    private final PermissionStaleService staleService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();

            if (staleService.shouldRecheck(username)) {

                try {
                    logger.info("用户 [" + username + "] 权限已过期或需例行校验，正在无感刷新Security Context...");

                    UserDetails newDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            newDetails,
                            authentication.getCredentials(),
                            newDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(newAuth);

                    staleService.clearStaleMark(username);
                    staleService.recordChecked(username);

                    logger.info("用户 [" + username + "] 权限刷新完成。");
                } catch (UsernameNotFoundException e) {
                    // 用户被禁用或已删除：销毁会话并登出，避免旧会话继续持有全部权限
                    logger.warn("用户 [" + username + "] 已不存在或已被禁用，销毁其会话");
                    SecurityContextHolder.clearContext();
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    ResponseTool.response(response, AjaxResult.UNAUTHORIZED);
                    return;
                } catch (Exception e) {
                    logger.error("用户 [" + username + "] 权限刷新失败", e);
                    ResponseTool.response(response, AjaxResult.err(e.getMessage()));
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
