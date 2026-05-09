package io.github.jiangood.openadmin.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestToolTest {

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        // 创建模拟的 HttpServletRequest
        mockRequest = Mockito.mock(HttpServletRequest.class);
    }

    @AfterEach
    void tearDown() {
        // 清理请求上下文
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testGetBaseUrlWithNullRequest() {
        String baseUrl = RequestTool.getBaseUrl(null);
        assertEquals("/", baseUrl);
    }

    @Test
    void testGetBaseUrlWithHttp() {
        // 模拟 HTTP 请求
        when(mockRequest.getScheme()).thenReturn("http");
        when(mockRequest.getServerName()).thenReturn("localhost");
        when(mockRequest.getServerPort()).thenReturn(8080);
        when(mockRequest.getHeader("x-forwarded-proto")).thenReturn(null);

        String baseUrl = RequestTool.getBaseUrl(mockRequest);
        assertEquals("http://localhost:8080", baseUrl);
    }

    @Test
    void testGetBaseUrlWithHttps() {
        // 模拟 HTTPS 请求
        when(mockRequest.getScheme()).thenReturn("https");
        when(mockRequest.getServerName()).thenReturn("example.com");
        when(mockRequest.getServerPort()).thenReturn(443);
        when(mockRequest.getHeader("x-forwarded-proto")).thenReturn(null);

        String baseUrl = RequestTool.getBaseUrl(mockRequest);
        assertEquals("https://example.com", baseUrl);
    }

    @Test
    void testGetBaseUrlWithXForwardedProto() {
        // 模拟带有 x-forwarded-proto 头的请求
        when(mockRequest.getScheme()).thenReturn("http");
        when(mockRequest.getServerName()).thenReturn("localhost");
        when(mockRequest.getServerPort()).thenReturn(8080);
        when(mockRequest.getHeader("x-forwarded-proto")).thenReturn("https");

        String baseUrl = RequestTool.getBaseUrl(mockRequest);
        assertEquals("https://localhost:8080", baseUrl);
    }

    @Test
    void testGetParamMap() {
        // 模拟请求参数
        Enumeration<String> paramNames = mock(Enumeration.class);
        when(mockRequest.getParameterNames()).thenReturn(paramNames);
        when(paramNames.hasMoreElements()).thenReturn(true, true, false);
        when(paramNames.nextElement()).thenReturn("name", "age");
        when(mockRequest.getParameter("name")).thenReturn("test");
        when(mockRequest.getParameter("age")).thenReturn("18");

        Map<String, String> paramMap = RequestTool.getParamMap(mockRequest);
        assertNotNull(paramMap);
        assertEquals(2, paramMap.size());
        assertEquals("test", paramMap.get("name"));
        assertEquals("18", paramMap.get("age"));
    }

    @Test
    void testGetHeaders() {
        // 模拟请求头
        Enumeration<String> headerNames = mock(Enumeration.class);
        when(mockRequest.getHeaderNames()).thenReturn(headerNames);
        when(headerNames.hasMoreElements()).thenReturn(true, true, false);
        when(headerNames.nextElement()).thenReturn("Content-Type", "User-Agent");
        when(mockRequest.getHeader("Content-Type")).thenReturn("application/json");
        when(mockRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        Map<String, String> headers = RequestTool.getHeaders(mockRequest);
        assertNotNull(headers);
        assertEquals(2, headers.size());
        assertEquals("application/json", headers.get("Content-Type"));
        assertEquals("Mozilla/5.0", headers.get("User-Agent"));
    }

    @Test
    void testCurrentRequest() {
        // 测试没有请求上下文的情况
        HttpServletRequest currentRequest = RequestTool.currentRequest();
        assertNull(currentRequest);

        // 测试有请求上下文的情况
        RequestAttributes requestAttributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        
        currentRequest = RequestTool.currentRequest();
        assertNotNull(currentRequest);
    }

}
