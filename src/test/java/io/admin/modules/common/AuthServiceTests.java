package io.admin.modules.common;

import io.admin.framework.config.SysProperties;
import io.admin.modules.system.ConfigConsts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.RSA;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @Mock
    private LoginAttemptService loginAttemptService;
    
    @Mock
    private SysProperties sysProperties;
    
    @Mock
    private CodeGenerator codeGenerator;
    
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        authService.loginAttemptService = loginAttemptService;
        authService.prop = sysProperties;
        authService.codeGenerator = codeGenerator;
    }

    @Test
    void testValidateSuccess() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        
        when(mockRequest.getParameter("username")).thenReturn("testuser");
        when(mockRequest.getParameter("captchaCode")).thenReturn("12345");
        when(mockRequest.getSession(false)).thenReturn(mock(HttpSession.class));
        when(loginAttemptService.isAccountLocked("testuser")).thenReturn(false);
        when(sysProperties.isCaptcha()).thenReturn(false);
        
        assertDoesNotThrow(() -> authService.validate(mockRequest));
        
        verify(loginAttemptService, times(1)).isAccountLocked("testuser");
    }

    @Test
    void testValidateAccountLocked() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        
        when(mockRequest.getParameter("username")).thenReturn("lockeduser");
        when(loginAttemptService.isAccountLocked("lockeduser")).thenReturn(true);
        when(sysProperties.getLoginLockMinutes()).thenReturn(30);
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            authService.validate(mockRequest);
        });
        
        assertTrue(exception.getMessage().contains("账户已被锁定"));
    }

    @Test
    void testValidateCaptchaRequired() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        
        when(mockRequest.getParameter("username")).thenReturn("testuser");
        when(mockRequest.getParameter("captchaCode")).thenReturn("12345");
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(loginAttemptService.isAccountLocked("testuser")).thenReturn(false);
        when(sysProperties.isCaptcha()).thenReturn(true);
        when(codeGenerator.verify(anyString(), eq("12345"))).thenReturn(true);
        
        assertDoesNotThrow(() -> authService.validate(mockRequest));
    }

    @Test
    void testDecodeWebPasswordSuccess() {
        String encryptedPassword = "encryptedPassword";
        String decryptedPassword = "decryptedPassword";
        
        // Mock ConfigConsts静态方法
        try (MockedStatic<ConfigConsts> mockedConfig = mockStatic(ConfigConsts.class)) {
            mockedConfig.when(() -> ConfigConsts.get(ConfigConsts.RSA_PRIVATE_KEY)).thenReturn("privateKey");
            mockedConfig.when(() -> ConfigConsts.get(ConfigConsts.RSA_PUBLIC_KEY)).thenReturn("publicKey");
            
            // Mock RSA解密过程
            try (MockedStatic<SecureUtil> mockedSecureUtil = mockStatic(SecureUtil.class)) {
                RSA mockRsa = mock(RSA.class);
                mockedSecureUtil.when(() -> SecureUtil.rsa(anyString(), anyString())).thenReturn(mockRsa);
                
                when(mockRsa.decryptStr(eq(encryptedPassword), any())).thenReturn(decryptedPassword);
                
                String result = authService.decodeWebPassword(encryptedPassword);
                assertEquals(decryptedPassword, result);
            }
        }
    }

    @Test
    void testOnSuccess() {
        String username = "testuser";
        
        authService.onSuccess(username);
        
        verify(loginAttemptService, times(1)).loginSucceeded(username);
    }

    @Test
    void testOnFail() {
        String username = "testuser";
        
        authService.onFail(username);
        
        verify(loginAttemptService, times(1)).loginFailed(username);
    }
}