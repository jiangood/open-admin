package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DecimalTool工具类的单元测试
 */
class DecimalToolTest {

    @Test
    void testToFixed2Double() {
        // 测试正常情况：正数
        assertEquals("123.46", DecimalTool.toFixed2(123.456));
        assertEquals("123.45", DecimalTool.toFixed2(123.454));
        
        // 测试负数
        assertEquals("-123.46", DecimalTool.toFixed2(-123.456));
        assertEquals("-123.45", DecimalTool.toFixed2(-123.454));
        
        // 测试零
        assertEquals("0.00", DecimalTool.toFixed2(0.0));
        assertEquals("0.00", DecimalTool.toFixed2(0.0));
        
        // 测试整数
        assertEquals("123.00", DecimalTool.toFixed2(123.0));
        assertEquals("456.00", DecimalTool.toFixed2(456.0));
        
        // 测试小数
        assertEquals("0.12", DecimalTool.toFixed2(0.12));
        assertEquals("0.01", DecimalTool.toFixed2(0.005));
        assertEquals("0.01", DecimalTool.toFixed2(0.006));
        
        // 测试大数
        assertEquals("1000000.00", DecimalTool.toFixed2(999999.999));
        
        // 测试null值
        assertEquals("", DecimalTool.toFixed2((Double) null));
    }

    @Test
    void testToFixed2Float() {
        // 测试正常情况：正数
        assertEquals("123.46", DecimalTool.toFixed2(123.456f));
        assertEquals("123.45", DecimalTool.toFixed2(123.454f));
        
        // 测试负数
        assertEquals("-123.46", DecimalTool.toFixed2(-123.456f));
        assertEquals("-123.45", DecimalTool.toFixed2(-123.454f));
        
        // 测试零
        assertEquals("0.00", DecimalTool.toFixed2(0.0f));
        assertEquals("0.00", DecimalTool.toFixed2(0f));
        
        // 测试整数
        assertEquals("123.00", DecimalTool.toFixed2(123.0f));
        assertEquals("456.00", DecimalTool.toFixed2(456.0f));
        
        // 测试小数
        assertEquals("0.12", DecimalTool.toFixed2(0.12f));
        assertEquals("0.01", DecimalTool.toFixed2(0.0051f));
        assertEquals("0.01", DecimalTool.toFixed2(0.006f));
        
        // 测试大数
        assertEquals("1000000.00", DecimalTool.toFixed2(999999.999f));
        
        // 测试null值
        assertNull(DecimalTool.toFixed2((Float) null));
    }

    @Test
    void testToFixed2BigDecimal() {
        // 测试正常情况：正数
        assertEquals("123.46", DecimalTool.toFixed2(new BigDecimal("123.456")));
        assertEquals("123.45", DecimalTool.toFixed2(new BigDecimal("123.454")));
        
        // 测试负数
        assertEquals("-123.46", DecimalTool.toFixed2(new BigDecimal("-123.456")));
        assertEquals("-123.45", DecimalTool.toFixed2(new BigDecimal("-123.454")));
        
        // 测试零
        assertEquals("0.00", DecimalTool.toFixed2(new BigDecimal("0.0")));
        assertEquals("0.00", DecimalTool.toFixed2(new BigDecimal("0")));
        
        // 测试整数
        assertEquals("123.00", DecimalTool.toFixed2(new BigDecimal("123")));
        assertEquals("456.00", DecimalTool.toFixed2(new BigDecimal("456")));
        
        // 测试小数
        assertEquals("0.12", DecimalTool.toFixed2(new BigDecimal("0.12")));
        assertEquals("0.01", DecimalTool.toFixed2(new BigDecimal("0.005")));
        assertEquals("0.01", DecimalTool.toFixed2(new BigDecimal("0.006")));
        
        // 测试大数
        assertEquals("1000000.00", DecimalTool.toFixed2(new BigDecimal("999999.999")));
        
        // 测试null值
        assertNull(DecimalTool.toFixed2((BigDecimal) null));
        
        // 测试高精度BigDecimal
        assertEquals("123.46", DecimalTool.toFixed2(new BigDecimal("123.456789")));
        assertEquals("0.00", DecimalTool.toFixed2(new BigDecimal("0.0001")));
    }
}
