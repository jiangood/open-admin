package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.ResponseTool;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();

                if (staleService.isStale(username)) {

                    logger.info("用户 [" + username + "] 权限已过期，正在无感刷新Security Context...");

                    UserDetails newDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            newDetails,
                            authentication.getCredentials(),
                            newDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(newAuth);

                    staleService.clearStaleMark(username);

                    logger.info("用户 [" + username + "] 权限刷新完成。");
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("处理失败" + e.getMessage());
            ResponseTool.response(response, AjaxResult.err(e.getMessage()));
        }
    }
}
