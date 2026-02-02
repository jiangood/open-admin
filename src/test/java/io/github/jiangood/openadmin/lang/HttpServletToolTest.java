package io.github.jiangood.openadmin.lang;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpServletTool工具类的单元测试
 */
class HttpServletToolTest {

    private MockedStatic<RequestContextHolder> requestContextHolderMock;
    private ServletRequestAttributes requestAttributes;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        // 模拟RequestContextHolder
        requestContextHolderMock = Mockito.mockStatic(RequestContextHolder.class);
        // 模拟ServletRequestAttributes
        requestAttributes = Mockito.mock(ServletRequestAttributes.class);
        // 模拟HttpServletRequest
        request = Mockito.mock(HttpServletRequest.class);
        // 模拟HttpServletResponse
        response = Mockito.mock(HttpServletResponse.class);
    }

    @AfterEach
    void tearDown() {
        // 关闭模拟
        if (requestContextHolderMock != null) {
            requestContextHolderMock.close();
        }
    }

    @Test
    void testGetRequestWithNullAttributes() {
        // 测试当RequestAttributes为null时的情况
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);
        // 当RequestAttributes为null时，应该抛出IllegalStateException异常
        assertThrows(IllegalStateException.class, () -> {
            HttpServletTool.getRequest();
        });
    }

    @Test
    void testGetRequestWithValidAttributes() {
        // 测试当RequestAttributes为非null时的情况
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
        Mockito.when(requestAttributes.getRequest()).thenReturn(request);
        HttpServletRequest result = HttpServletTool.getRequest();
        assertNotNull(result);
        assertEquals(request, result);
    }

    @Test
    void testGetResponseWithNullAttributes() {
        // 测试当RequestAttributes为null时的情况
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);
        // 当RequestAttributes为null时，应该抛出IllegalStateException异常
        assertThrows(IllegalStateException.class, () -> {
            HttpServletTool.getResponse();
        });
    }

    @Test
    void testGetResponseWithValidAttributes() {
        // 测试当RequestAttributes为非null时的情况
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
        Mockito.when(requestAttributes.getResponse()).thenReturn(response);
        HttpServletResponse result = HttpServletTool.getResponse();
        assertNotNull(result);
        assertEquals(response, result);
    }
}
