package io.github.jiangood.openadmin.framework.auth;

import io.github.jiangood.openadmin.framework.config.security.LoginUser;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import io.github.jiangood.openadmin.util.BrowserFingerprintTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuthOperationLoggerTest {

    private SysLogService sysLogService;
    private AuthOperationLogger logger;

    @BeforeEach
    void setUp() {
        sysLogService = mock(SysLogService.class);
        logger = new AuthOperationLogger(sysLogService);
    }

    @Test
    void loginSuccessShouldRecordOperatorAndDetail() {
        logger.loginSuccess(request(), "admin", loginUser(), 12);

        SysLog sysLog = capture();
        assertEquals(AuthOperationLogger.OPERATION_LOGIN, sysLog.getOperation());
        assertEquals("账号「admin」登录成功", sysLog.getDetail());
        assertEquals("管理员", sysLog.getUsername());
        assertEquals("u1", sysLog.getUserId());
        assertEquals("127.0.0.1", sysLog.getIp());
        assertEquals(12, sysLog.getDuration().intValue());
        assertTrue(sysLog.getSuccess());
    }

    @Test
    void loginShouldRecordBrowserFingerprint() {
        MockHttpServletRequest request = request();
        request.addHeader(BrowserFingerprintTool.HEADER, "fp-123");

        logger.loginSuccess(request, "admin", loginUser(), 12);

        assertEquals("fp-123", capture().getFingerprint());
    }

    @Test
    void loginFailedShouldRecordAccountAndReason() {
        logger.loginFailed(request(), "admin", "账号或密码错误", 8);

        SysLog sysLog = capture();
        assertEquals(AuthOperationLogger.OPERATION_LOGIN_FAILED, sysLog.getOperation());
        assertEquals("账号「admin」登录失败：账号或密码错误", sysLog.getDetail());
        assertEquals("admin", sysLog.getUsername());
        assertNull(sysLog.getUserId());
        assertEquals("账号或密码错误", sysLog.getError());
        assertFalse(sysLog.getSuccess());
    }

    @Test
    void logoutShouldRecordOperator() {
        logger.logout(request(), loginUser(), 5);

        SysLog sysLog = capture();
        assertEquals(AuthOperationLogger.OPERATION_LOGOUT, sysLog.getOperation());
        assertEquals("账号「admin」退出登录", sysLog.getDetail());
        assertEquals("管理员", sysLog.getUsername());
        assertEquals("u1", sysLog.getUserId());
        assertTrue(sysLog.getSuccess());
    }

    @Test
    void logoutWithoutLoginUserShouldBeSkipped() {
        logger.logout(request(), null, 5);

        verify(sysLogService, never()).saveOperationLogAsync(any());
    }

    @Test
    void detailShouldBeTruncatedToColumnLength() {
        String longAccount = "a".repeat(600);
        logger.loginFailed(request(), longAccount, "账号或密码错误", 1);

        SysLog sysLog = capture();
        assertEquals(500, sysLog.getDetail().length());
        assertEquals(50, sysLog.getUsername().length());
    }

    private SysLog capture() {
        ArgumentCaptor<SysLog> captor = ArgumentCaptor.forClass(SysLog.class);
        verify(sysLogService).saveOperationLogAsync(captor.capture());
        return captor.getValue();
    }

    private MockHttpServletRequest request() {
        return new MockHttpServletRequest();
    }

    private LoginUser loginUser() {
        LoginUser user = new LoginUser("admin", "n/a", Set.of());
        user.setId("u1");
        user.setName("管理员");
        return user;
    }
}
