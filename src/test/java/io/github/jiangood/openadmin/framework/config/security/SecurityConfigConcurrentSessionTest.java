package io.github.jiangood.openadmin.framework.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 校验 SecurityConfig 真的把 401 JSON 策略接线到了 ConcurrentSessionFilter。
 * <p>
 * 单测 {@link ConcurrentSessionExpiredStrategyTest} 只覆盖策略本身，本测试防止
 * 「策略写对了但忘记 / 被误删 registration」类回归。
 */
@SpringBootTest
class SecurityConfigConcurrentSessionTest {

    @Autowired
    @Qualifier("securityFilterChain")
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private SecurityHolder securityHolder;

    @Test
    void sessionAuthenticationStrategyShouldRejectSecondLoginByDefault() {
        SessionAuthenticationStrategy strategy = securityHolder.getSharedObject(SessionAuthenticationStrategy.class);
        assertNotNull(strategy, "共享对象中未注册 SessionAuthenticationStrategy");

        String principal = "concurrent-login-test-user";
        sessionRegistry.registerNewSession("existing-session", principal);
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.getSession(true); // ChangeSessionIdAuthenticationStrategy 需要已有会话
            MockHttpServletResponse response = new MockHttpServletResponse();
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "n/a");

            assertThrows(SessionAuthenticationException.class,
                    () -> strategy.onAuthentication(authentication, request, response),
                    "默认应阻止第二个会话登录（sys.max-sessions-prevents-login=true）");
        } finally {
            sessionRegistry.removeSessionInformation("existing-session");
        }
    }

    @Test
    void sessionRegistryShouldBeExposedAsBean() {
        assertNotNull(sessionRegistry, "SessionRegistry 未注册为 Bean，在线用户列表与强制下线将无法工作");

        sessionRegistry.registerNewSession("test-session", "test-principal");
        assertNotNull(sessionRegistry.getSessionInformation("test-session"));

        sessionRegistry.removeSessionInformation("test-session");
        assertNull(sessionRegistry.getSessionInformation("test-session"));
    }

    @Test
    void filterChainShouldContainSessionRestoreFilter() {
        boolean present = ((DefaultSecurityFilterChain) securityFilterChain).getFilters().stream()
                .anyMatch(SessionRestoreFilter.class::isInstance);
        assertTrue(present, "过滤链中未注册 SessionRestoreFilter，应用重启后在线用户与并发会话控制无法恢复");
    }

    @Test
    void filterChainShouldUseJson401ExpiredStrategy() throws Exception {
        ConcurrentSessionFilter filter = ((DefaultSecurityFilterChain) securityFilterChain).getFilters().stream()
                .filter(ConcurrentSessionFilter.class::isInstance)
                .map(ConcurrentSessionFilter.class::cast)
                .findFirst()
                .orElseThrow(() -> new AssertionError("过滤链中未注册 ConcurrentSessionFilter"));

        Field field = ConcurrentSessionFilter.class.getDeclaredField("sessionInformationExpiredStrategy");
        field.setAccessible(true);
        SessionInformationExpiredStrategy strategy = (SessionInformationExpiredStrategy) field.get(filter);

        assertEquals(ConcurrentSessionExpiredStrategy.class, strategy.getClass(), "并发会话失效策略未接线");

        MockHttpServletResponse response = new MockHttpServletResponse();
        SessionInformationExpiredEvent event = new SessionInformationExpiredEvent(
                new SessionInformation("admin", "session-1", new Date()), new MockHttpServletRequest(), response);
        strategy.onExpiredSessionDetected(event);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
    }
}
