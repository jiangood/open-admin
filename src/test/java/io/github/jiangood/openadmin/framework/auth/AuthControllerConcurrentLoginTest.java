package io.github.jiangood.openadmin.framework.auth;

import io.github.jiangood.openadmin.framework.auth.dto.LoginReq;
import io.github.jiangood.openadmin.framework.auth.online.OnlineUserRegistry;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.security.SecurityHolder;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 并发会话超限时 {@link AuthController} 的提示文案。
 * <p>
 * 默认 {@code sys.max-sessions-prevents-login=true}：新登录被拒，必须提示「已在别处登录」，
 * 不能误导成「账号或密码错误」；关闭该开关（踢出旧会话）时才会退回通用错误文案。
 */
class AuthControllerConcurrentLoginTest {

    @Test
    void shouldUseBlockedMessageWhenPreventLoginEnabled() {
        SystemProperties properties = new SystemProperties();
        properties.setMaxSessionsPreventsLogin(true);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> newController(properties).login(loginReq(), new MockHttpServletRequest(), new MockHttpServletResponse()));

        assertEquals(properties.getConcurrentLoginBlockedMessage(), e.getMessage());
    }

    @Test
    void shouldFallbackToGenericMessageWhenPreventLoginDisabled() {
        SystemProperties properties = new SystemProperties();
        properties.setMaxSessionsPreventsLogin(false);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> newController(properties).login(loginReq(), new MockHttpServletRequest(), new MockHttpServletResponse()));

        assertEquals("账号或密码错误", e.getMessage());
    }

    private AuthController newController(SystemProperties properties) {
        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "n/a", List.of());
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        SessionAuthenticationStrategy sessionStrategy = mock(SessionAuthenticationStrategy.class);
        doThrow(new SessionAuthenticationException("Maximum sessions of 1 for this principal exceeded"))
                .when(sessionStrategy).onAuthentication(any(), any(), any());

        SecurityHolder securityHolder = new SecurityHolder();
        Map<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(AuthenticationManager.class, authenticationManager);
        sharedObjects.put(SessionAuthenticationStrategy.class, sessionStrategy);
        sharedObjects.put(SecurityContextRepository.class, mock(SecurityContextRepository.class));
        securityHolder.setSharedObjects(sharedObjects);

        return new AuthController(securityHolder, mock(LoginAttemptService.class), properties,
                mock(OnlineUserRegistry.class), mock(SysUserService.class), mock(AuthOperationLogger.class));
    }

    private LoginReq loginReq() {
        LoginReq req = new LoginReq();
        req.setUsername("admin");
        req.setPassword(encodePassword("Admin@123456"));
        return req;
    }

    /**
     * 与前端 {@code login.jsx} 的 encodePassword 保持一致：每个字符加下标和 2 后 Base64。
     */
    private String encodePassword(String plain) {
        byte[] source = plain.getBytes(StandardCharsets.UTF_8);
        byte[] encoded = new byte[source.length];
        for (int i = 0; i < source.length; i++) {
            encoded[i] = (byte) (source[i] + i + 2);
        }
        return Base64.getEncoder().encodeToString(encoded);
    }
}
