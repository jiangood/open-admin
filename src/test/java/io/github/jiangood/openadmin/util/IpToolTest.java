package io.github.jiangood.openadmin.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IpToolTest {

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = mock(HttpServletRequest.class);
    }

    @Test
    void testGetIpWithNullRequest() {
        String ip = IpTool.getIp(null);
        assertEquals("127.0.0.1", ip);
    }

    @Test
    void testGetIpWithLocalhost() {
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(mockRequest.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(mockRequest.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(mockRequest.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(mockRequest.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");

        String ip = IpTool.getIp(mockRequest);
        assertEquals("127.0.0.1", ip);
    }

    @Test
    void testGetIpWithRemoteAddr() {
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(mockRequest.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(mockRequest.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(mockRequest.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(mockRequest.getRemoteAddr()).thenReturn("192.168.1.100");

        String ip = IpTool.getIp(mockRequest);
        assertEquals("192.168.1.100", ip);
    }

    @Test
    void testGetAddress() {
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(mockRequest.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(mockRequest.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(mockRequest.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        String address = IpTool.getAddress(mockRequest);
        assertEquals("内网", address);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"127.0.0.1", "192.168.1.1"})
    void testGetLocationWithNonPublicIp(String ip) {
        assertEquals("内网", IpTool.getLocation(ip));
    }

    @Test
    void testGetLocationWithPublicIp() {
        // 这里我们不实际调用外部API，而是测试缓存机制
        String ip = "8.8.8.8";
        
        // 第一次调用，应该会尝试获取地理位置
        String location1 = IpTool.getLocation(ip);
        assertNotNull(location1);
        
        // 第二次调用，应该从缓存中获取
        String location2 = IpTool.getLocation(ip);
        assertEquals(location1, location2);
    }

}
