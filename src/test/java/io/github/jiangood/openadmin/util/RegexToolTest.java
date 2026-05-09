package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * RegexTool工具类的单元测试
 */
class RegexToolTest {

    @Test
    void testFindMatch() {
        // 测试正常情况：找到匹配的组
        String result1 = RegexTool.findMatch("a(b)c", "abc", 1);
        assertEquals("b", result1);
        
        String result2 = RegexTool.findMatch("\\d+", "abc123def", 0);
        assertEquals("123", result2);
        
        String result3 = RegexTool.findMatch("([a-z]+)(\\d+)", "abc123", 1);
        assertEquals("abc", result3);
        
        String result4 = RegexTool.findMatch("([a-z]+)(\\d+)", "abc123", 2);
        assertEquals("123", result4);
        
        // 测试多个组的情况
        String result5 = RegexTool.findMatch("(\\d{4})-(\\d{2})-(\\d{2})", "2023-12-25", 1);
        assertEquals("2023", result5);
        
        String result6 = RegexTool.findMatch("(\\d{4})-(\\d{2})-(\\d{2})", "2023-12-25", 2);
        assertEquals("12", result6);
        
        String result7 = RegexTool.findMatch("(\\d{4})-(\\d{2})-(\\d{2})", "2023-12-25", 3);
        assertEquals("25", result7);
        
        // 测试不匹配的情况
        String result8 = RegexTool.findMatch("\\d+", "abcdef", 0);
        assertNull(result8);
        
        String result9 = RegexTool.findMatch("a(b)c", "def", 1);
        assertNull(result9);
        
        // 测试组索引超出范围
        String result10 = RegexTool.findMatch("a(b)c", "abc", 2);
        assertNull(result10);
        
        // 测试组索引为0的情况（返回整个匹配）
        String result11 = RegexTool.findMatch("a(b)c", "abc", 0);
        assertEquals("abc", result11);
        
        // 测试空字符串
        String result12 = RegexTool.findMatch("\\d+", "", 0);
        assertNull(result12);
        
        // 测试特殊字符匹配
        String result13 = RegexTool.findMatch("\\s+", "hello   world", 0);
        assertEquals("   ", result13);
        
        String result14 = RegexTool.findMatch("[@#$]+", "test@email#address", 0);
        assertEquals("@", result14);
        
        // 测试邮箱匹配
        String result15 = RegexTool.findMatch("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+)\\.([a-zA-Z]{2,})", "test@example.com", 1);
        assertEquals("test", result15);
        
        String result16 = RegexTool.findMatch("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+)\\.([a-zA-Z]{2,})", "test@example.com", 2);
        assertEquals("example", result16);
        
        String result17 = RegexTool.findMatch("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+)\\.([a-zA-Z]{2,})", "test@example.com", 3);
        assertEquals("com", result17);
    }

    @Test
    void testFindFirstMatch() {
        // 测试正常情况：找到第一个匹配
        String result1 = RegexTool.findFirstMatch("(\\d+)", "abc123def456");
        assertEquals("123", result1);
        
        String result2 = RegexTool.findFirstMatch("([a-z]+)", "123abc456def");
        assertEquals("abc", result2);
        
        String result3 = RegexTool.findFirstMatch("(\\w+)", "test@example.com");
        assertEquals("test", result3);
        
        // 测试邮箱匹配
        String result4 = RegexTool.findFirstMatch("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})", "Contact us at test@example.com for more info");
        assertEquals("test@example.com", result4);
        
        // 测试URL匹配
        String result5 = RegexTool.findFirstMatch("(https?://[\\w.-]+)", "Visit https://example.com for more information");
        assertEquals("https://example.com", result5);
        
        // 测试不匹配的情况
        String result6 = RegexTool.findFirstMatch("(\\d+)", "abcdef");
        assertNull(result6);
        
        String result7 = RegexTool.findFirstMatch("([A-Z]+)", "abc123");
        assertNull(result7);
        
        // 测试空字符串
        String result8 = RegexTool.findFirstMatch("(\\d+)", "");
        assertNull(result8);
        
        // 测试特殊字符匹配
        String result9 = RegexTool.findFirstMatch("(\\s+)", "hello   world");
        assertEquals("   ", result9);
        
        // 测试电话号码匹配
        String result10 = RegexTool.findFirstMatch("(\\d{3}-\\d{4})", "Call me at 123-4567 or 987-6543");
        assertEquals("123-4567", result10);
        
        // 测试日期匹配
        String result11 = RegexTool.findFirstMatch("(\\d{4}-\\d{2}-\\d{2})", "Today is 2023-12-25");
        assertEquals("2023-12-25", result11);
        
        // 测试IP地址匹配
        String result12 = RegexTool.findFirstMatch("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})", "Server IP: 192.168.1.1");
        assertEquals("192.168.1.1", result12);
        
        // 测试十六进制颜色匹配
        String result13 = RegexTool.findFirstMatch("(#[0-9a-fA-F]{6})", "Background color: #FF5733 and text color: #333333");
        assertEquals("#FF5733", result13);
        
        // 测试时间匹配
        String result14 = RegexTool.findFirstMatch("(\\d{2}:\\d{2}:\\d{2})", "Meeting at 14:30:00");
        assertEquals("14:30:00", result14);
    }
}
