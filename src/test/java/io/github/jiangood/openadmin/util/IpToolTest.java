package io.github.jiangood.openadmin.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class IpToolTest {

    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = Mockito.mock(HttpServletRequest.class);
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

    @Test
    void testGetLocationWithEmptyIp() {
        String location = IpTool.getLocation(null);
        assertEquals("内网", location);
    }

    @Test
    void testGetLocationWithLocalIp() {
        String location = IpTool.getLocation("127.0.0.1");
        assertEquals("内网", location);
    }

    @Test
    void testGetLocationWithLanIp() {
        String location = IpTool.getLocation("192.168.1.1");
        assertEquals("内网", location);
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
