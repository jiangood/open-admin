package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.framework.auth.online.OnlineUserRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 应用重启后，Tomcat 会从 {@code server.servlet.session.store-dir} 恢复 HttpSession，
 * 但 {@link SessionRegistry} 与 {@link OnlineUserRegistry} 都是内存态，会丢失对应记录。
 * <p>
 * 这里在认证请求进入时按需补登记：恢复在线用户列表，并让「最大并发会话数」重新生效。
 * 正常登录的请求不需要补登记（登录时 {@code SessionAuthenticationStrategy} 已登记）。
 */
@Slf4j
@RequiredArgsConstructor
public class SessionRestoreFilter extends OncePerRequestFilter {

    private final SessionRegistry sessionRegistry;
    private final OnlineUserRegistry onlineUserRegistry;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        restoreIfNeeded(request);
        filterChain.doFilter(request, response);
    }

    private void restoreIfNeeded(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        String sessionId = session.getId();
        boolean sessionMissing = sessionRegistry.getSessionInformation(sessionId) == null;
        boolean onlineMissing = onlineUserRegistry.findBySessionId(sessionId) == null;
        if (!sessionMissing && !onlineMissing) {
            return;
        }

        if (sessionMissing) {
            sessionRegistry.registerNewSession(sessionId, loginUser);
        }
        if (onlineMissing) {
            onlineUserRegistry.restore(request, loginUser);
        }
        log.debug("应用重启后恢复会话登记: {}", sessionId);
    }
}
