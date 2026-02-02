package io.github.jiangood.openadmin.framework.config.security.refresh;

import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.ResponseTool;
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

            // 1. 确保用户已登录且已认证
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName(); // 获取当前登录用户名

                // 2. 检查该用户是否被标记为权限过期
                if (staleService.isStale(username)) {

                    logger.info("用户 [" + username + "] 权限已过期，正在无感刷新Security Context...");

                    // 3. 从数据库重新加载最新的 UserDetails
                    UserDetails newDetails = userDetailsService.loadUserByUsername(username);

                    // 4. 构建新的认证对象 (使用最新的Authorities)
                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            newDetails,
                            authentication.getCredentials(), // 保持原有的Credentials (如密码哈希或JWT)
                            newDetails.getAuthorities()     // 【关键】使用最新的权限信息
                    );

                    // 5. 更新安全上下文，覆盖旧的权限信息
                    SecurityContextHolder.getContext().setAuthentication(newAuth);

                    // 6. 清除权限过期标记
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
