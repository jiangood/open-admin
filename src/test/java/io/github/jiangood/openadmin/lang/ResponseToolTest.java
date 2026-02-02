package io.github.jiangood.openadmin.lang;

import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseToolTest {

    private HttpServletResponse mockResponse;
    private HttpServletRequest mockRequest;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        // 创建模拟的 HttpServletResponse
        mockResponse = Mockito.mock(HttpServletResponse.class);
        // 创建模拟的 HttpServletRequest
        mockRequest = Mockito.mock(HttpServletRequest.class);
        // 创建 StringWriter 来捕获响应输出
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testSetDownloadHeader() throws IOException {
        String filename = "test.txt";
        String contentType = "text/plain";
        
        // 调用方法
        ResponseTool.setDownloadHeader(filename, contentType, mockResponse);
        
        // 验证设置的头信息
        verify(mockResponse).setContentType(contentType);
        verify(mockResponse).setHeader("Content-Disposition", "attachment;filename=test.txt");
        verify(mockResponse).setHeader("Access-Control-Expose-Headers", "content-disposition");
    }

    @Test
    void testSetDownloadExcelHeader() throws IOException {
        String filename = "test.xlsx";
        
        // 调用方法
        ResponseTool.setDownloadExcelHeader(filename, mockResponse);
        
        // 验证设置的头信息
        verify(mockResponse).setContentType(ResponseTool.CONTENT_TYPE_EXCEL);
        verify(mockResponse).setHeader("Content-Disposition", "attachment;filename=test.xlsx");
        verify(mockResponse).setHeader("Access-Control-Expose-Headers", "content-disposition");
    }

    @Test
    void testSetCrossDomain() {
        String origin = "http://example.com";
        when(mockRequest.getHeader("Origin")).thenReturn(origin);
        
        // 调用方法
        ResponseTool.setCrossDomain(mockRequest, mockResponse);
        
        // 验证设置的头信息
        verify(mockResponse).setHeader("Access-Control-Allow-Origin", origin);
        verify(mockResponse).setHeader("Access-Control-Allow-Credentials", "true");
        verify(mockResponse).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        verify(mockResponse).setHeader("Access-Control-Allow-Headers", "X-Requested-With,Content-Type,jwt,Authorization");
        verify(mockResponse).addHeader("Access-Control-Max-Age", "3600");
    }

    @Test
    void testResponseJson() throws IOException {
        // 创建测试数据
        AjaxResult result = AjaxResult.ok().msg("Success");
        
        // 调用方法
        ResponseTool.responseJson(mockResponse, result);
        
        // 验证设置的内容类型
        verify(mockResponse).setCharacterEncoding("UTF-8");
        verify(mockResponse).setContentType("application/json;charset=utf-8");
        
        // 验证响应内容
        String responseContent = responseWriter.toString();
        assertNotNull(responseContent);
        assertTrue(responseContent.contains("Success"));
    }

    @Test
    void testResponseExceptionError() throws IOException {
        Integer code = 500;
        String message = "Internal Server Error";
        
        // 调用方法
        ResponseTool.responseExceptionError(mockResponse, code, message);
        
        // 验证设置的内容类型和状态码
        verify(mockResponse).setCharacterEncoding("UTF-8");
        verify(mockResponse).setContentType("application/json;charset=utf-8");
        verify(mockResponse).setStatus(code);
        
        // 验证响应内容
        String responseContent = responseWriter.toString();
        assertNotNull(responseContent);
        assertTrue(responseContent.contains(message));
    }

    @Test
    void testResponseHtml() throws IOException {
        String html = "<html><body><h1>Test</h1></body></html>";
        
        // 调用方法
        ResponseTool.responseHtml(mockResponse, html);
        
        // 验证设置的内容类型
        verify(mockResponse).setContentType("text/html;charset=UTF-8");
        
        // 验证响应内容
        String responseContent = responseWriter.toString();
        assertEquals(html, responseContent);
    }

    @Test
    void testResponse() throws IOException {
        // 创建测试数据
        AjaxResult result = AjaxResult.ok().msg("Success");
        
        // 调用方法
        ResponseTool.response(mockResponse, result);
        
        // 验证设置的内容类型
        verify(mockResponse).setContentType("application/json;charset=UTF-8");
        
        // 验证响应内容
        String responseContent = responseWriter.toString();
        assertNotNull(responseContent);
        assertTrue(responseContent.contains("Success"));
    }

    @Test
    void testResponseJsonWithNullData() throws IOException {
        // 调用方法，传入null数据
        ResponseTool.responseJson(mockResponse, null);
        
        // 验证设置的内容类型
        verify(mockResponse).setCharacterEncoding("UTF-8");
        verify(mockResponse).setContentType("application/json;charset=utf-8");
        
        // 验证响应内容为"null"
        String responseContent = responseWriter.toString();
        assertNotNull(responseContent);
        assertEquals("null", responseContent);
    }

    @Test
    void testResponseHtmlBlockWithNullContent() throws IOException {
        String title = "Test Title";
        
        // 调用方法
        ResponseTool.responseHtmlBlock(mockResponse, title, null);
        
        // 验证设置的内容类型
        verify(mockResponse).setContentType("text/html;charset=utf-8");
        
        // 验证响应内容为空
        String responseContent = responseWriter.toString();
        assertEquals("", responseContent);
    }

    @Test
    void testResponseHtmlBlockWithNullTitle() throws IOException {
        String content = "<p>Test Content</p>";
        
        // 调用方法
        ResponseTool.responseHtmlBlock(mockResponse, null, content);
        
        // 验证设置的内容类型
        verify(mockResponse).setContentType("text/html;charset=utf-8");
        
        // 验证响应内容为空
        String responseContent = responseWriter.toString();
        assertEquals("", responseContent);
    }

    @Test
    void testResponseHtmlBlockWithValidContent() throws IOException {
        String title = "Test Title";
        String content = "<p>Test Content</p>";
        
        // 调用方法
        ResponseTool.responseHtmlBlock(mockResponse, title, content);
        
        // 验证设置的内容类型
        verify(mockResponse).setContentType("text/html;charset=utf-8");
        
        // 验证响应内容包含标题和内容
        String responseContent = responseWriter.toString();
        assertNotNull(responseContent);
        assertTrue(responseContent.contains(title));
        assertTrue(responseContent.contains(content));
    }

    @Test
    void testResponseHtmlBlockWithImgContent() throws IOException {
        String title = "Test Title";
        String content = "<p>Test Content</p><img src=\"https://example.com/sysFile/preview/test.jpg\" alt=\"Test\" />";
        
        // 调用方法
        ResponseTool.responseHtmlBlock(mockResponse, title, content);
        
        // 验证设置的内容类型
        verify(mockResponse).setContentType("text/html;charset=utf-8");
        
        // 验证响应内容包含标题和处理后的图片标签（移除了前缀）
        String responseContent = responseWriter.toString();
        assertNotNull(responseContent);
        assertTrue(responseContent.contains(title));
        assertTrue(responseContent.contains("<img src=\"/sysFile/preview/test.jpg\" alt=\"Test\" />"));
    }

    @Test
    void testSetDownloadHeaderWithSpecialCharacters() throws IOException {
        String filename = "测试文件.txt";
        String contentType = "text/plain";
        
        // 调用方法
        ResponseTool.setDownloadHeader(filename, contentType, mockResponse);
        
        // 验证设置的头信息
        verify(mockResponse).setContentType(contentType);
        verify(mockResponse).setHeader("Content-Disposition", "attachment;filename=%E6%B5%8B%E8%AF%95%E6%96%87%E4%BB%B6.txt");
        verify(mockResponse).setHeader("Access-Control-Expose-Headers", "content-disposition");
    }

    @Test
    void testSetDownloadExcelHeaderWithSpecialCharacters() throws IOException {
        String filename = "测试表格.xlsx";
        
        // 调用方法
        ResponseTool.setDownloadExcelHeader(filename, mockResponse);
        
        // 验证设置的头信息
        verify(mockResponse).setContentType(ResponseTool.CONTENT_TYPE_EXCEL);
        verify(mockResponse).setHeader("Content-Disposition", "attachment;filename=%E6%B5%8B%E8%AF%95%E8%A1%A8%E6%A0%BC.xlsx");
        verify(mockResponse).setHeader("Access-Control-Expose-Headers", "content-disposition");
    }

} 
