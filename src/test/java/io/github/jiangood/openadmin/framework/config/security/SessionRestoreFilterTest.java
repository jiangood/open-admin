package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.framework.auth.online.OnlineUser;
import io.github.jiangood.openadmin.framework.auth.online.OnlineUserRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistryImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SessionRestoreFilterTest {

    private final SessionRegistryImpl sessionRegistry = new SessionRegistryImpl();
    private final OnlineUserRegistry onlineUserRegistry = new OnlineUserRegistry();
    private final SessionRestoreFilter filter = new SessionRestoreFilter(sessionRegistry, onlineUserRegistry);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRestoreSessionRegistryAndOnlineUserAfterRestart() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = request(session);
        authenticate(loginUser());

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
        });

        assertNotNull(sessionRegistry.getSessionInformation(session.getId()));
        OnlineUser onlineUser = onlineUserRegistry.findBySessionId(session.getId());
        assertNotNull(onlineUser);
        assertEquals("admin", onlineUser.getUsername());
        assertEquals("管理员", onlineUser.getName());
    }

    @Test
    void shouldPreserveLoginMetadataFromRestoredSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = request(session);
        LoginUser loginUser = loginUser();
        authenticate(loginUser);

        // 登录时的在线记录：保留登录时间/IP/UA
        onlineUserRegistry.record(request, loginUser);
        LocalDateTime loginTime = onlineUserRegistry.findBySessionId(session.getId()).getLoginTime();
        assertNotNull(loginTime);

        // 模拟应用重启：内存注册表全部清空，但 HttpSession 从磁盘恢复（属性仍在）
        SessionRegistryImpl restartedSessionRegistry = new SessionRegistryImpl();
        OnlineUserRegistry restartedOnlineRegistry = new OnlineUserRegistry();
        new SessionRestoreFilter(restartedSessionRegistry, restartedOnlineRegistry)
                .doFilter(request, new MockHttpServletResponse(), (req, res) -> {
                });

        assertNotNull(restartedSessionRegistry.getSessionInformation(session.getId()));
        OnlineUser restored = restartedOnlineRegistry.findBySessionId(session.getId());
        assertNotNull(restored);
        assertEquals(loginTime, restored.getLoginTime());
        assertEquals("127.0.0.1", restored.getIp());
        assertEquals("JUnit Browser", restored.getUserAgent());
    }

    @Test
    void shouldSkipUnauthenticatedRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = request(session);

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
        });

        assertNull(sessionRegistry.getSessionInformation(session.getId()));
        assertNull(onlineUserRegistry.findBySessionId(session.getId()));
    }

    private MockHttpServletRequest request(MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit Browser");
        return request;
    }

    private LoginUser loginUser() {
        LoginUser user = new LoginUser("admin", "",
                List.of(new SimpleGrantedAuthority("ROLE_admin")));
        user.setId("u1");
        user.setName("管理员");
        user.setUnitId("unit-1");
        user.setUnitName("总部");
        user.setOrgId("org-1");
        user.setOrgName("研发部");
        return user;
    }

    private void authenticate(LoginUser user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
}
