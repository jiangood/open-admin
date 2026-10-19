package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.util.JsonTool;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.web.session.SessionInformationExpiredEvent;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 并发会话被顶下线时的响应契约测试。
 * <p>
 * 回归背景：Spring Security 默认策略返回 HTTP 200 + 纯文本 "This session has been expired ..."，
 * 前端 HttpClient 无法识别为登录失效而静默失败。本测试锁定「401 + AjaxResult JSON」的契约。
 */
class ConcurrentSessionExpiredStrategyTest {

    private SessionInformationExpiredEvent expiredEvent(MockHttpServletResponse response) {
        SessionInformation sessionInformation = new SessionInformation("admin", "session-1", new Date());
        return new SessionInformationExpiredEvent(sessionInformation, new MockHttpServletRequest(), response);
    }

    @Test
    void shouldReturn401JsonWithDefaultMessage() throws Exception {
        SystemProperties systemProperties = new SystemProperties();
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ConcurrentSessionExpiredStrategy(systemProperties).onExpiredSessionDetected(expiredEvent(response));

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"), "响应当为 JSON，实际为 " + response.getContentType());

        Map<String, Object> body = JsonTool.jsonToMapQuietly(response.getContentAsString());
        assertEquals(Boolean.FALSE, body.get("success"));
        assertEquals(401, ((Number) body.get("code")).intValue());
        assertEquals(systemProperties.getConcurrentLoginMessage(), body.get("message"));
        // 锁定与前端约定的线上契约（字段名字面量断言，常量改名不会让前端静默失效）
        assertEquals("CONCURRENT_LOGIN", body.get("loginExpiredReason"));
        assertEquals("CONCURRENT_LOGIN", ConcurrentSessionExpiredStrategy.REASON_CONCURRENT_LOGIN);
        assertEquals("loginExpiredReason", ConcurrentSessionExpiredStrategy.EXT_KEY_REASON);
    }

    @Test
    void shouldUseConfiguredMessage() throws Exception {
        SystemProperties systemProperties = new SystemProperties();
        systemProperties.setConcurrentLoginMessage("账号已在其它设备登录，请重新登录");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ConcurrentSessionExpiredStrategy(systemProperties).onExpiredSessionDetected(expiredEvent(response));

        Map<String, Object> body = JsonTool.jsonToMapQuietly(response.getContentAsString());
        assertEquals(401, response.getStatus());
        assertEquals("账号已在其它设备登录，请重新登录", body.get("message"));
    }

    @Test
    void shouldNotReturnSpringSecurityDefaultPlainText() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ConcurrentSessionExpiredStrategy(new SystemProperties()).onExpiredSessionDetected(expiredEvent(response));

        assertFalse(response.getContentAsString().contains("This session has been expired"),
                "不应再返回 Spring Security 默认的纯文本提示");
    }
}
