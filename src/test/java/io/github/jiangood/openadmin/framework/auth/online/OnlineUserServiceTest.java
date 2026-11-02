package io.github.jiangood.openadmin.framework.auth.online;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineUserServiceTest {

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

        service.kick(row.getId());

        assertTrue(registry.list().isEmpty());
        SessionInformation information = sessionRegistry.getSessionInformation(session.getId());
        assertNotNull(information);
        assertTrue(information.isExpired());
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

}
