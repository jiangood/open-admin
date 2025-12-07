package io.admin.modules.common;

import io.admin.common.dto.AjaxResult;
import io.admin.framework.config.SysProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import cn.hutool.captcha.generator.CodeGenerator;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthControllerTests {

    private MockMvc mockMvc;
    
    private AuthController authController;
    
    @MockBean
    private SysProperties sysProperties;
    
    @MockBean
    private CodeGenerator codeGenerator;

    @BeforeEach
    void setUp() {
        authController = new AuthController(sysProperties, codeGenerator);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testLogout() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        
        AjaxResult result = authController.logout(mockRequest);
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(mockSession, times(1)).invalidate();
    }

    @Test
    void testCaptcha() throws Exception {
        // 创建模拟的HttpSession和HttpServletResponse
        HttpSession mockSession = mock(HttpSession.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        
        when(mockSession.getId()).thenReturn("testSessionId");
        when(mockResponse.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
                // Not needed for test
            }
        });
        
        // 由于验证码方法涉及外部库和图形生成，这里仅验证方法调用
        assertDoesNotThrow(() -> {
            authController.captcha(mockSession, mockResponse);
        });
        
        verify(mockResponse).setContentType("image/png");
        verify(mockResponse).setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
    }
}