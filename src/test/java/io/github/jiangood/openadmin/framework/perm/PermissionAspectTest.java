package io.github.jiangood.openadmin.framework.perm;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionAspectTest {

    private PermissionAspect permissionAspect;

    @BeforeEach
    void setUp() {
        permissionAspect = new PermissionAspect();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCheckPermission_withValidPermission_shouldPass() {
        LoginUser loginUser = mock(LoginUser.class);
        when(loginUser.getPermissions()).thenReturn(Set.of("user:query"));

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(loginUser);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        HasPermission hasPermission = mock(HasPermission.class);
        when(hasPermission.value()).thenReturn("user:query");

        assertDoesNotThrow(() -> permissionAspect.checkPermission(hasPermission));
    }

    @Test
    void testCheckPermission_withoutPermission_shouldThrow() {
        LoginUser loginUser = mock(LoginUser.class);
        when(loginUser.getPermissions()).thenReturn(Set.of("user:query"));

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(loginUser);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        HasPermission hasPermission = mock(HasPermission.class);
        when(hasPermission.value()).thenReturn("admin:access");

        assertThrows(AccessDeniedException.class, () -> permissionAspect.checkPermission(hasPermission));
    }

    @Test
    void testCheckPermission_withoutAuthentication_shouldThrow() {
        SecurityContextHolder.getContext().setAuthentication(null);

        HasPermission hasPermission = mock(HasPermission.class);
        when(hasPermission.value()).thenReturn("user:query");

        assertThrows(AccessDeniedException.class, () -> permissionAspect.checkPermission(hasPermission));
    }

    @Test
    void testCheckPermission_withUnauthenticatedUser_shouldThrow() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        HasPermission hasPermission = mock(HasPermission.class);
        when(hasPermission.value()).thenReturn("user:query");

        assertThrows(AccessDeniedException.class, () -> permissionAspect.checkPermission(hasPermission));
    }

    @Test
    void testCheckPermission_withGrantedAuthority_shouldPass() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("notLoginUser");
        when(authentication.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("role:admin")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        HasPermission hasPermission = mock(HasPermission.class);
        when(hasPermission.value()).thenReturn("role:admin");

        assertDoesNotThrow(() -> permissionAspect.checkPermission(hasPermission));
    }

    @Test
    void testCheckPermission_withGrantedAuthority_shouldThrow() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("notLoginUser");
        when(authentication.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("role:user")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        HasPermission hasPermission = mock(HasPermission.class);
        when(hasPermission.value()).thenReturn("role:admin");

        assertThrows(AccessDeniedException.class, () -> permissionAspect.checkPermission(hasPermission));
    }
}