package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.framework.auth.online.OnlineUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 会话落盘（server.servlet.session.persistent）依赖 JDK 序列化，
 * 这里锁定登录用户、SecurityContext 与在线记录的序列化契约。
 */
class SessionSerializationTest {

    @Test
    void securityContextWithLoginUserShouldRoundTrip() throws Exception {
        LoginUser user = new LoginUser("admin", "encoded-password",
                List.of(new SimpleGrantedAuthority("ROLE_admin"), new SimpleGrantedAuthority("sys:user:view")));
        user.setId("u1");
        user.setName("管理员");
        user.setUnitId("unit-1");
        user.setUnitName("总部");
        user.setOrgId("org-1");
        user.setOrgName("研发部");
        user.setLastLoginTime(LocalDateTime.of(2026, 11, 6, 9, 30));
        user.eraseCredentials();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        SecurityContext restored = roundTrip(context);

        LoginUser restoredUser = assertInstanceOf(LoginUser.class, restored.getAuthentication().getPrincipal());
        assertEquals("admin", restoredUser.getUsername());
        assertNull(restoredUser.getPassword(), "序列化前应已擦除密码");
        assertEquals("u1", restoredUser.getId());
        assertEquals("管理员", restoredUser.getName());
        assertEquals("总部", restoredUser.getUnitName());
        assertEquals(LocalDateTime.of(2026, 11, 6, 9, 30), restoredUser.getLastLoginTime());
        assertEquals(Set.of("ROLE_admin", "sys:user:view"), restoredUser.getPermissions());
        assertTrue(restoredUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_admin".equals(authority.getAuthority())));
    }

    @Test
    void onlineUserShouldRoundTrip() throws Exception {
        OnlineUser user = new OnlineUser();
        user.setId("online-1");
        user.setSessionId("session-1");
        user.setUserId("u1");
        user.setUsername("admin");
        user.setName("管理员");
        user.setUnitId("unit-1");
        user.setUnitName("总部");
        user.setOrgId("org-1");
        user.setOrgName("研发部");
        user.setIp("127.0.0.1");
        user.setInnerIp(true);
        user.setUserAgent("JUnit Browser");
        user.setLoginTime(LocalDateTime.of(2026, 11, 6, 9, 30));

        OnlineUser restored = roundTrip(user);

        assertEquals("online-1", restored.getId());
        assertEquals("session-1", restored.getSessionId());
        assertEquals("admin", restored.getUsername());
        assertEquals("127.0.0.1", restored.getIp());
        assertTrue(restored.isInnerIp());
        assertEquals("JUnit Browser", restored.getUserAgent());
        assertEquals(LocalDateTime.of(2026, 11, 6, 9, 30), restored.getLoginTime());
        assertFalse(restored.isCurrent());
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T value) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
            return (T) in.readObject();
        }
    }
}
