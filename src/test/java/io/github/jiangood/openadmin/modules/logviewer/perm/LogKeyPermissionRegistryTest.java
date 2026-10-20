package io.github.jiangood.openadmin.modules.logviewer.perm;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogKeyPermissionRegistryTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static LogKeyPermissionProvider provider(Map<String, String> entries) {
        return () -> entries;
    }

    private static void login(String... permissions) {
        LoginUser user = new LoginUser("u", "p", java.util.Arrays.stream(permissions)
                .map(SimpleGrantedAuthority::new).toList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @Test
    void resolvePermission_shouldMatchLongestPrefix() {
        LogKeyPermissionRegistry registry = new LogKeyPermissionRegistry(List.of(
                provider(Map.of("job", "job:read")),
                provider(Map.of("job/export", "job:export"))
        ));

        assertEquals("job:read", registry.resolvePermission("job/123"));
        assertEquals("job:read", registry.resolvePermission("/job/123"));
        assertEquals("job:export", registry.resolvePermission("job/export/1"));
        assertNull(registry.resolvePermission("other/123"));
    }

    @Test
    void checkPermission_shouldAllowWhenPermissionPresent() {
        LogKeyPermissionRegistry registry = new LogKeyPermissionRegistry(List.of(
                provider(Map.of("job", "job:read"))));
        login("job:read");

        assertDoesNotThrow(() -> registry.checkPermission("job/123"));
    }

    @Test
    void checkPermission_shouldAllowAdminWildcard() {
        LogKeyPermissionRegistry registry = new LogKeyPermissionRegistry(List.of(
                provider(Map.of("job", "job:read"))));
        login("*");

        assertDoesNotThrow(() -> registry.checkPermission("job/123"));
    }

    @Test
    void checkPermission_shouldDenyWhenPermissionMissing() {
        LogKeyPermissionRegistry registry = new LogKeyPermissionRegistry(List.of(
                provider(Map.of("job", "job:read"))));
        login("job:update");

        assertThrows(AccessDeniedException.class, () -> registry.checkPermission("job/123"));
    }

    @Test
    void checkPermission_shouldDenyUnregisteredKey() {
        LogKeyPermissionRegistry registry = new LogKeyPermissionRegistry(List.of(
                provider(Map.of("job", "job:read"))));
        login("*");

        // 未注册前缀即使管理员也不放行，避免通用日志接口变成任意文件读取
        assertThrows(AccessDeniedException.class, () -> registry.checkPermission("secret/1"));
    }

    @Test
    void checkPermission_shouldDenyWhenNotLogin() {
        LogKeyPermissionRegistry registry = new LogKeyPermissionRegistry(List.of(
                provider(Map.of("job", "job:read"))));

        assertThrows(AccessDeniedException.class, () -> registry.checkPermission("job/123"));
    }
}
