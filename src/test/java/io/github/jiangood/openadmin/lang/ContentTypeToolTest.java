package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ContentTypeTool工具类的单元测试
 */
class ContentTypeToolTest {

    @Test
    void testIsVideo() {
        // 测试视频类型
        assertTrue(ContentTypeTool.isVideo("video/mp4"));
        assertTrue(ContentTypeTool.isVideo("video/x-msvideo"));
        assertTrue(ContentTypeTool.isVideo("video/quicktime"));
        assertTrue(ContentTypeTool.isVideo("video/x-ms-wmv"));
        assertTrue(ContentTypeTool.isVideo("video/webm"));
        assertTrue(ContentTypeTool.isVideo("video/x-matroska"));

        // 测试非视频类型
        assertFalse(ContentTypeTool.isVideo("image/jpeg"));
        assertFalse(ContentTypeTool.isVideo("audio/mpeg"));
        assertFalse(ContentTypeTool.isVideo("application/pdf"));
        assertFalse(ContentTypeTool.isVideo("text/plain"));

        // 测试null值
        assertFalse(ContentTypeTool.isVideo(null));

        // 测试空字符串
        assertFalse(ContentTypeTool.isVideo(""));
    }

    @Test
    void testGetContentTypeByExtension() {
        // 测试图片类型
        assertEquals(MediaType.IMAGE_JPEG_VALUE, ContentTypeTool.getContentTypeByExtension("jpg"));
        assertEquals(MediaType.IMAGE_JPEG_VALUE, ContentTypeTool.getContentTypeByExtension("jpeg"));
        assertEquals(MediaType.IMAGE_PNG_VALUE, ContentTypeTool.getContentTypeByExtension("png"));
        assertEquals(MediaType.IMAGE_GIF_VALUE, ContentTypeTool.getContentTypeByExtension("gif"));
        assertEquals("image/bmp", ContentTypeTool.getContentTypeByExtension("bmp"));
        assertEquals("image/webp", ContentTypeTool.getContentTypeByExtension("webp"));
        assertEquals("image/svg+xml", ContentTypeTool.getContentTypeByExtension("svg"));

        // 测试视频类型
        assertEquals("video/mp4", ContentTypeTool.getContentTypeByExtension("mp4"));
        assertEquals("video/x-msvideo", ContentTypeTool.getContentTypeByExtension("avi"));
        assertEquals("video/quicktime", ContentTypeTool.getContentTypeByExtension("mov"));
        assertEquals("video/x-ms-wmv", ContentTypeTool.getContentTypeByExtension("wmv"));
        assertEquals("video/webm", ContentTypeTool.getContentTypeByExtension("webm"));
        assertEquals("video/x-matroska", ContentTypeTool.getContentTypeByExtension("mkv"));

        // 测试音频类型
        assertEquals("audio/mpeg", ContentTypeTool.getContentTypeByExtension("mp3"));
        assertEquals("audio/wav", ContentTypeTool.getContentTypeByExtension("wav"));
        assertEquals("audio/ogg", ContentTypeTool.getContentTypeByExtension("ogg"));

        // 测试文档类型
        assertEquals(MediaType.APPLICATION_PDF_VALUE, ContentTypeTool.getContentTypeByExtension("pdf"));
        assertEquals(MediaType.TEXT_PLAIN_VALUE, ContentTypeTool.getContentTypeByExtension("txt"));
        assertEquals(MediaType.TEXT_HTML_VALUE, ContentTypeTool.getContentTypeByExtension("html"));
        assertEquals(MediaType.TEXT_HTML_VALUE, ContentTypeTool.getContentTypeByExtension("htm"));
        assertEquals(MediaType.APPLICATION_XML_VALUE, ContentTypeTool.getContentTypeByExtension("xml"));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, ContentTypeTool.getContentTypeByExtension("json"));

        // 测试Office文档类型
        assertEquals("application/msword", ContentTypeTool.getContentTypeByExtension("doc"));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ContentTypeTool.getContentTypeByExtension("docx"));
        assertEquals("application/vnd.ms-excel", ContentTypeTool.getContentTypeByExtension("xls"));
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ContentTypeTool.getContentTypeByExtension("xlsx"));
        assertEquals("application/vnd.ms-powerpoint", ContentTypeTool.getContentTypeByExtension("ppt"));
        assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation", ContentTypeTool.getContentTypeByExtension("pptx"));

        // 测试大小写不敏感
        assertEquals(MediaType.IMAGE_JPEG_VALUE, ContentTypeTool.getContentTypeByExtension("JPG"));
        assertEquals(MediaType.IMAGE_PNG_VALUE, ContentTypeTool.getContentTypeByExtension("PNG"));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, ContentTypeTool.getContentTypeByExtension("JSON"));

        // 测试默认类型
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, ContentTypeTool.getContentTypeByExtension("unknown"));

        // 测试null值
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, ContentTypeTool.getContentTypeByExtension(null));

        // 测试空字符串
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, ContentTypeTool.getContentTypeByExtension(""));
    }
}
