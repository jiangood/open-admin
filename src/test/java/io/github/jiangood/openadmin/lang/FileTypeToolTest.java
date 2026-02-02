package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FileTypeTool工具类的单元测试
 */
class FileTypeToolTest {

    @Test
    void testIsImage() {
        // 测试正常图片类型
        assertTrue(FileTypeTool.isImage("test.png"));
        assertTrue(FileTypeTool.isImage("test.jpg"));
        assertTrue(FileTypeTool.isImage("test.git"));
        assertTrue(FileTypeTool.isImage("test.jpeg"));

        // 测试大小写不敏感
        assertTrue(FileTypeTool.isImage("test.PNG"));
        assertTrue(FileTypeTool.isImage("test.JPG"));
        assertTrue(FileTypeTool.isImage("test.GIT"));
        assertTrue(FileTypeTool.isImage("test.JPEG"));

        // 测试非图片类型
        assertFalse(FileTypeTool.isImage("test.txt"));
        assertFalse(FileTypeTool.isImage("test.doc"));
        assertFalse(FileTypeTool.isImage("test.pdf"));
        assertFalse(FileTypeTool.isImage("test.mp4"));

        // 测试null值
        assertFalse(FileTypeTool.isImage(null));

        // 测试空字符串
        assertFalse(FileTypeTool.isImage(""));
    }

    @Test
    void testIsOffice() {
        // 测试正常Office文档类型
        assertTrue(FileTypeTool.isOffice("test.doc"));
        assertTrue(FileTypeTool.isOffice("test.docx"));
        assertTrue(FileTypeTool.isOffice("test.xls"));
        assertTrue(FileTypeTool.isOffice("test.xlsx"));
        assertTrue(FileTypeTool.isOffice("test.ppt"));
        assertTrue(FileTypeTool.isOffice("test.pptx"));

        // 测试大小写不敏感
        assertTrue(FileTypeTool.isOffice("test.DOC"));
        assertTrue(FileTypeTool.isOffice("test.DOCX"));
        assertTrue(FileTypeTool.isOffice("test.XLS"));
        assertTrue(FileTypeTool.isOffice("test.XLSX"));
        assertTrue(FileTypeTool.isOffice("test.PPT"));
        assertTrue(FileTypeTool.isOffice("test.PPTX"));

        // 测试非Office文档类型
        assertFalse(FileTypeTool.isOffice("test.txt"));
        assertFalse(FileTypeTool.isOffice("test.pdf"));
        assertFalse(FileTypeTool.isOffice("test.png"));
        assertFalse(FileTypeTool.isOffice("test.mp4"));

        // 测试null值
        assertFalse(FileTypeTool.isOffice(null));

        // 测试空字符串
        assertFalse(FileTypeTool.isOffice(""));
    }
}
