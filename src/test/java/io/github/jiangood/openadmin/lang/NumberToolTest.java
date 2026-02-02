package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * NumberTool工具类的单元测试
 */
class NumberToolTest {

    @Test
    void testFormatNumber() {
        // 测试正常情况：正数
        assertEquals("123.46", NumberTool.formatNumber(123.456, 2));
        assertEquals("123", NumberTool.formatNumber(123.456, 0));
        assertEquals("123.456", NumberTool.formatNumber(123.456, 3));
        
        // 测试负数
        assertEquals("-123.46", NumberTool.formatNumber(-123.456, 2));
        assertEquals("-123", NumberTool.formatNumber(-123.456, 0));
        
        // 测试零
        assertEquals("0", NumberTool.formatNumber(0, 2));
        assertEquals("0", NumberTool.formatNumber(0.0, 2));
        assertEquals("0", NumberTool.formatNumber(0.000, 3));
        
        // 测试末尾有多余的0的情况
        assertEquals("123", NumberTool.formatNumber(123.000, 2));
        assertEquals("123.5", NumberTool.formatNumber(123.500, 2));
        
        // 测试小数位数为0
        assertEquals("123", NumberTool.formatNumber(123.456, 0));
        assertEquals("124", NumberTool.formatNumber(123.556, 0));
        
        // 测试较大的小数位数
        assertEquals("123.456789", NumberTool.formatNumber(123.456789, 6));
        
        // 测试四舍五入
        assertEquals("123.46", NumberTool.formatNumber(123.455, 2));
        assertEquals("123.45", NumberTool.formatNumber(123.454, 2));
        
        // 测试小数位数为负数（应该抛出异常）
        assertThrows(IllegalArgumentException.class, () -> {
            NumberTool.formatNumber(123.456, -1);
        });
    }

    @Test
    void testTryGetNumber() {
        // 测试正常情况：整数字符串
        Number result1 = NumberTool.tryGetNumber("123");
        assertNotNull(result1);
        assertTrue(result1 instanceof Integer);
        assertEquals(123, result1.intValue());
        
        // 测试正常情况：小数字符串
        Number result2 = NumberTool.tryGetNumber("123.45");
        assertNotNull(result2);
        assertTrue(result2 instanceof Float);
        assertEquals(123.45f, result2.floatValue(), 0.001);
        
        // 测试非数字字符串（应该返回null）
        assertNull(NumberTool.tryGetNumber("test"));
        assertNull(NumberTool.tryGetNumber("123a"));
        assertNull(NumberTool.tryGetNumber("a123"));
        
        // 测试空字符串（应该返回null）
        assertNull(NumberTool.tryGetNumber(""));
        
        // 测试null值（应该返回null）
        assertNull(NumberTool.tryGetNumber(null));
        
        // 测试负数
        Number result3 = NumberTool.tryGetNumber("-123");
        assertNotNull(result3);
        assertTrue(result3 instanceof Integer);
        assertEquals(-123, result3.intValue());
        
        Number result4 = NumberTool.tryGetNumber("-123.45");
        assertNotNull(result4);
        assertTrue(result4 instanceof Float);
        assertEquals(-123.45f, result4.floatValue(), 0.001);
        
        // 测试零
        Number result5 = NumberTool.tryGetNumber("0");
        assertNotNull(result5);
        assertTrue(result5 instanceof Integer);
        assertEquals(0, result5.intValue());
        
        Number result6 = NumberTool.tryGetNumber("0.0");
        assertNotNull(result6);
        assertTrue(result6 instanceof Float);
        assertEquals(0.0f, result6.floatValue(), 0.001);
    }
}
