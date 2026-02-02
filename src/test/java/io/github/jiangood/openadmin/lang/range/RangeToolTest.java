package io.github.jiangood.openadmin.lang.range;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class RangeToolTest {

    @Test
    void testToStrRange() {
        // 测试空字符串
        Range<String> emptyRange = RangeTool.toStrRange("");
        assertNull(emptyRange.getStart());
        assertNull(emptyRange.getEnd());
        
        // 测试只有开始值
        Range<String> startOnlyRange = RangeTool.toStrRange("apple");
        assertEquals("apple", startOnlyRange.getStart());
        assertNull(startOnlyRange.getEnd());
        
        // 测试完整范围
        Range<String> fullRange = RangeTool.toStrRange("apple/banana");
        assertEquals("apple", fullRange.getStart());
        assertEquals("banana", fullRange.getEnd());
    }

    @Test
    void testToStrRangeWithInvalidFormat() {
        // 测试格式错误的字符串（超过2个部分）
        assertThrows(IllegalStateException.class, () -> {
            RangeTool.toStrRange("a/b/c");
        });
    }

    @Test
    void testToBigDecimalRange() {
        // 测试空字符串
        Range<BigDecimal> emptyRange = RangeTool.toBigDecimalRange("");
        assertNull(emptyRange.getStart());
        assertNull(emptyRange.getEnd());
        
        // 测试只有开始值
        Range<BigDecimal> startOnlyRange = RangeTool.toBigDecimalRange("10.5");
        assertEquals(new BigDecimal("10.5"), startOnlyRange.getStart());
        assertNull(startOnlyRange.getEnd());
        
        // 测试完整范围
        Range<BigDecimal> fullRange = RangeTool.toBigDecimalRange("10.5/20.75");
        assertEquals(new BigDecimal("10.5"), fullRange.getStart());
        assertEquals(new BigDecimal("20.75"), fullRange.getEnd());
    }

    @Test
    void testToLongRange() {
        // 测试空字符串
        Range<Long> emptyRange = RangeTool.toLongRange("");
        assertNull(emptyRange.getStart());
        assertNull(emptyRange.getEnd());
        
        // 测试只有开始值
        Range<Long> startOnlyRange = RangeTool.toLongRange("100");
        assertEquals(100L, startOnlyRange.getStart());
        assertNull(startOnlyRange.getEnd());
        
        // 测试完整范围
        Range<Long> fullRange = RangeTool.toLongRange("100/200");
        assertEquals(100L, fullRange.getStart());
        assertEquals(200L, fullRange.getEnd());
    }

    @Test
    void testToIntRange() {
        // 测试空字符串
        Range<Integer> emptyRange = RangeTool.toIntRange("");
        assertNull(emptyRange.getStart());
        assertNull(emptyRange.getEnd());
        
        // 测试只有开始值
        Range<Integer> startOnlyRange = RangeTool.toIntRange("10");
        assertEquals(10, startOnlyRange.getStart());
        assertNull(startOnlyRange.getEnd());
        
        // 测试完整范围
        Range<Integer> fullRange = RangeTool.toIntRange("10/20");
        assertEquals(10, fullRange.getStart());
        assertEquals(20, fullRange.getEnd());
    }

    @Test
    void testToDateRange() {
        // 测试空字符串
        Range<Date> emptyRange = RangeTool.toDateRange("");
        assertNull(emptyRange.getStart());
        assertNull(emptyRange.getEnd());
        
        // 测试只有开始值
        Range<Date> startOnlyRange = RangeTool.toDateRange("2023-01-01");
        assertNotNull(startOnlyRange.getStart());
        assertNull(startOnlyRange.getEnd());
        
        // 测试完整范围
        Range<Date> fullRange = RangeTool.toDateRange("2023-01-01/2023-12-31");
        assertNotNull(fullRange.getStart());
        assertNotNull(fullRange.getEnd());
        assertTrue(fullRange.getStart().before(fullRange.getEnd()));
    }

}
