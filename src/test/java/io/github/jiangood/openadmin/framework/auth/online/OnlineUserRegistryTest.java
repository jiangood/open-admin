package io.github.jiangood.openadmin.framework.auth.online;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnlineUserRegistryTest {

    private LoginUser loginUser() {
        LoginUser user = new LoginUser("admin", "pwd", List.of());
        user.setId("user-1");
        user.setName("管理员");
        user.setUnitName("总部");
        user.setOrgName("研发部");
        return user;
    }

    private MockHttpServletRequest request(MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit Browser");
        return request;
    }

    @Test
    void shouldRecordAndRemoveOnlineUser() {
        OnlineUserRegistry registry = new OnlineUserRegistry();
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = request(session);

        registry.record(request, loginUser());

        List<OnlineUser> list = registry.list();
        assertEquals(1, list.size());

        OnlineUser onlineUser = list.get(0);
        assertNotNull(onlineUser.getId());
        assertEquals(session.getId(), onlineUser.getSessionId());
        assertEquals("admin", onlineUser.getUsername());
        assertEquals("管理员", onlineUser.getName());
        assertEquals("总部", onlineUser.getUnitName());
        assertEquals("研发部", onlineUser.getOrgName());
        assertEquals("127.0.0.1", onlineUser.getIp());
        assertEquals("JUnit Browser", onlineUser.getUserAgent());
        assertNotNull(onlineUser.getLoginTime());

        assertNotNull(registry.findById(onlineUser.getId()));
        assertNotNull(registry.findBySessionId(session.getId()));

        registry.remove(session.getId());
        assertTrue(registry.list().isEmpty());
        assertNull(registry.findById(onlineUser.getId()));
    }

    @Test
    void shouldRemoveWhenSessionDestroyed() {
        OnlineUserRegistry registry = new OnlineUserRegistry();
        MockHttpSession session = new MockHttpSession();
        registry.record(request(session), loginUser());
        assertEquals(1, registry.list().size());

        registry.onSessionDestroyed(new HttpSessionDestroyedEvent(session));

        assertTrue(registry.list().isEmpty());
    }

}
