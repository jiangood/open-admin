package io.github.jiangood.openadmin.framework.auth.online;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineUserServiceTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private LoginUser loginUser() {
        LoginUser user = new LoginUser("admin", "pwd", List.of());
        user.setId("user-1");
        user.setName("管理员");
        return user;
    }

    @Test
    void shouldPageAndKick() {
        OnlineUserRegistry registry = new OnlineUserRegistry();
        SessionRegistryImpl sessionRegistry = new SessionRegistryImpl();

        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        request.addHeader("User-Agent", "JUnit Browser");

        LoginUser loginUser = loginUser();
        registry.record(request, loginUser);
        sessionRegistry.registerNewSession(session.getId(), loginUser);

        OnlineUserService service = new OnlineUserService(registry, sessionRegistry);

        Page<OnlineUser> page = service.page(null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        OnlineUser row = page.getContent().get(0);
        assertNotNull(row.getLastActiveTime());
        assertFalse(row.isCurrent());
        assertTrue(row.isInnerIp());

        service.kick(row.getId());

        assertTrue(registry.list().isEmpty());
        SessionInformation information = sessionRegistry.getSessionInformation(session.getId());
        assertNotNull(information);
        assertTrue(information.isExpired());
    }

    @Test
    void shouldMarkInnerIp() {
        OnlineUserRegistry registry = new OnlineUserRegistry();
        SessionRegistryImpl sessionRegistry = new SessionRegistryImpl();
        LoginUser user = loginUser();

        MockHttpSession innerSession = new MockHttpSession();
        MockHttpServletRequest innerRequest = new MockHttpServletRequest();
        innerRequest.setSession(innerSession);
        innerRequest.setRemoteAddr("172.18.0.1");
        registry.record(innerRequest, user);
        sessionRegistry.registerNewSession(innerSession.getId(), user);

        MockHttpSession publicSession = new MockHttpSession();
        MockHttpServletRequest publicRequest = new MockHttpServletRequest();
        publicRequest.setSession(publicSession);
        publicRequest.setRemoteAddr("8.8.8.8");
        registry.record(publicRequest, user);
        sessionRegistry.registerNewSession(publicSession.getId(), user);

        OnlineUserService service = new OnlineUserService(registry, sessionRegistry);
        List<OnlineUser> rows = service.page(null, PageRequest.of(0, 10)).getContent();

        OnlineUser inner = rows.stream().filter(r -> "172.18.0.1".equals(r.getIp())).findFirst().orElseThrow();
        OnlineUser pub = rows.stream().filter(r -> "8.8.8.8".equals(r.getIp())).findFirst().orElseThrow();
        assertTrue(inner.isInnerIp());
        assertFalse(pub.isInnerIp());
    }

    @Test
    void shouldFilterByKeyword() {
        OnlineUserRegistry registry = new OnlineUserRegistry();
        SessionRegistryImpl sessionRegistry = new SessionRegistryImpl();

        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        LoginUser loginUser = loginUser();
        registry.record(request, loginUser);
        sessionRegistry.registerNewSession(session.getId(), loginUser);

        OnlineUserService service = new OnlineUserService(registry, sessionRegistry);

        assertEquals(1, service.page("管理", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, service.page("admin", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(0, service.page("不存在", PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void shouldCleanExpiredSessionOnPage() {
        OnlineUserRegistry registry = new OnlineUserRegistry();
        SessionRegistryImpl sessionRegistry = new SessionRegistryImpl();

        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        LoginUser loginUser = loginUser();
        registry.record(request, loginUser);
        sessionRegistry.registerNewSession(session.getId(), loginUser);
        sessionRegistry.getSessionInformation(session.getId()).expireNow();

        OnlineUserService service = new OnlineUserService(registry, sessionRegistry);

        assertEquals(0, service.page(null, PageRequest.of(0, 10)).getTotalElements());
        assertTrue(registry.list().isEmpty());
    }

    @Test
    void shouldListOnlyCurrentUsersSessions() {
        OnlineUserRegistry registry = new OnlineUserRegistry();
        SessionRegistryImpl sessionRegistry = new SessionRegistryImpl();
        OnlineUserService service = new OnlineUserService(registry, sessionRegistry);

        MockHttpServletRequest mine = recordSession(registry, sessionRegistry, "user-1", "admin");
        recordSession(registry, sessionRegistry, "user-2", "other");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mine));

        List<OnlineUser> sessions = service.listByUser("user-1");
        assertEquals(1, sessions.size());
        assertEquals("admin", sessions.get(0).getUsername());
        assertTrue(sessions.get(0).isCurrent());

        assertEquals(1, service.listByUser("user-2").size());
        assertFalse(service.listByUser("user-2").get(0).isCurrent());
        assertTrue(service.listByUser(null).isEmpty());
    }

    @Test
    void shouldKickOwnSessionsOnly() {
        OnlineUserRegistry registry = new OnlineUserRegistry();
        SessionRegistryImpl sessionRegistry = new SessionRegistryImpl();
        OnlineUserService service = new OnlineUserService(registry, sessionRegistry);

        MockHttpServletRequest current = recordSession(registry, sessionRegistry, "user-1", "admin");
        MockHttpServletRequest other = recordSession(registry, sessionRegistry, "user-1", "admin");
        MockHttpServletRequest stranger = recordSession(registry, sessionRegistry, "user-2", "other");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(current));

        String currentId = registry.findBySessionId(current.getSession().getId()).getId();
        String otherId = registry.findBySessionId(other.getSession().getId()).getId();
        String strangerId = registry.findBySessionId(stranger.getSession().getId()).getId();

        service.kickOwn(otherId, "user-1");
        assertTrue(sessionRegistry.getSessionInformation(other.getSession().getId()).isExpired());
        assertNull(registry.findBySessionId(other.getSession().getId()));

        assertThrows(IllegalStateException.class, () -> service.kickOwn(currentId, "user-1"));
        assertThrows(IllegalStateException.class, () -> service.kickOwn(strangerId, "user-1"));
        assertThrows(IllegalArgumentException.class, () -> service.kickOwn("missing", "user-1"));
    }

    private MockHttpServletRequest recordSession(OnlineUserRegistry registry,
                                                 SessionRegistryImpl sessionRegistry,
                                                 String userId,
                                                 String username) {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        request.addHeader("User-Agent", "JUnit Browser");

        LoginUser user = new LoginUser(username, "pwd", List.of());
        user.setId(userId);
        user.setName(username);
        registry.record(request, user);
        sessionRegistry.registerNewSession(session.getId(), user);
        return request;
    }

}
