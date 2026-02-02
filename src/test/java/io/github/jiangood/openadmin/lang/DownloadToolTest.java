package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletOutputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;

/**
 * DownloadTool工具类的单元测试
 */
class DownloadToolTest {

    @Test
    void testSetDownloadParam() {
        // 准备测试数据
        String fileName = "test.txt";
        long contentLength = 1024;

        // 模拟 HttpServletResponse
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // 执行设置参数操作
        DownloadTool.setDownloadParam(fileName, contentLength, response);

        // 验证响应头设置
        verify(response).reset();
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"test.txt\"");
        verify(response).setHeader("Access-Control-Allow-Origin", "*");
        verify(response).setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        verify(response).setContentType("application/octet-stream;charset=UTF-8");
        verify(response).addHeader("Content-Length", String.valueOf(contentLength));
    }

    @Test
    void testSetDownloadParamWithZeroContentLength() {
        // 准备测试数据
        String fileName = "test.txt";
        long contentLength = 0;

        // 模拟 HttpServletResponse
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // 执行设置参数操作
        DownloadTool.setDownloadParam(fileName, contentLength, response);

        // 验证响应头设置
        verify(response).reset();
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"test.txt\"");
        verify(response).setHeader("Access-Control-Allow-Origin", "*");
        verify(response).setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        verify(response).setContentType("application/octet-stream;charset=UTF-8");
        // 验证 contentLength 为 0 时不设置 Content-Length 头
        verify(response, never()).addHeader(eq("Content-Length"), anyString());
    }

    @Test
    void testSetDownloadParamWithNegativeContentLength() {
        // 准备测试数据
        String fileName = "test.txt";
        long contentLength = -1;

        // 模拟 HttpServletResponse
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // 执行设置参数操作
        DownloadTool.setDownloadParam(fileName, contentLength, response);

        // 验证响应头设置
        verify(response).reset();
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"test.txt\"");
        verify(response).setHeader("Access-Control-Allow-Origin", "*");
        verify(response).setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        verify(response).setContentType("application/octet-stream;charset=UTF-8");
        // 验证 contentLength 为负数时不设置 Content-Length 头
        verify(response, never()).addHeader(eq("Content-Length"), anyString());
    }

    @Test
    void testDownloadWithByteArrayThrowsException() throws Exception {
        // 准备测试数据
        String fileName = "test.txt";
        byte[] fileBytes = "Hello".getBytes();

        // 模拟 HttpServletResponse，使其在获取输出流时抛出异常
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IOException("Output stream error"));

        // 验证是否抛出 IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            DownloadTool.download(fileName, fileBytes, response);
        });
    }

    @Test
    void testDownloadWithInputStreamThrowsException() throws Exception {
        // 准备测试数据
        String fileName = "test.txt";
        InputStream inputStream = new ByteArrayInputStream("Hello".getBytes());
        long fileSize = 5;

        // 模拟 HttpServletResponse，使其在获取输出流时抛出异常
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IOException("Output stream error"));

        // 验证是否抛出 IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            DownloadTool.download(fileName, inputStream, fileSize, response);
        });
    }
}
