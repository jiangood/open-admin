package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FontTool工具类的单元测试
 */
class FontToolTest {

    @Test
    void testGetDefaultFontPath() {
        // 测试获取默认字体路径
        String fontPath = FontTool.getDefaultFontPath();
        // 字体路径可能为null（如果系统中没有安装指定的字体），但方法应该正常执行
        // 这里只验证方法不会抛出异常
        assertDoesNotThrow(() -> FontTool.getDefaultFontPath());
    }

    @Test
    void testGetDefaultFontName() {
        // 测试获取默认字体名称
        String fontName = FontTool.getDefaultFontName();
        // 字体名称可能为null（如果系统中没有安装指定的字体），但方法应该正常执行
        // 这里只验证方法不会抛出异常
        assertDoesNotThrow(() -> FontTool.getDefaultFontName());
    }

    @Test
    void testGetAllFontName() {
        // 测试获取所有可用字体名称
        Set<String> fontNames = FontTool.getAllFontName();
        // 字体名称集合应该不为null，且至少包含系统默认的一些字体
        assertNotNull(fontNames);
        // 验证方法不会抛出异常
        assertDoesNotThrow(() -> FontTool.getAllFontName());
    }
}
