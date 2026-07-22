package io.github.jiangood.openadmin.util.range;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class RangeToolTest {

    @Test
    void testToStrRangeWithFullRange() {
        // 测试完整范围
        Range<String> range = RangeTool.toStrRange("start/end");
        assertNotNull(range);
        assertEquals("start", range.getStart());
        assertEquals("end", range.getEnd());
    }

    @Test
    void testToStrRangeWithOnlyStart() {
        // 测试只有开始值的范围
        Range<String> range = RangeTool.toStrRange("start/");
        assertNotNull(range);
        assertEquals("start", range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testToStrRangeWithOnlyEnd() {
        // 测试只有结束值的范围
        Range<String> range = RangeTool.toStrRange("/end");
        assertNotNull(range);
        assertNull(range.getStart());
        assertEquals("end", range.getEnd());
    }

    @Test
    void testToStrRangeWithEmptyString() {
        // 测试空字符串
        Range<String> range = RangeTool.toStrRange("");
        assertNotNull(range);
        assertNull(range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testToStrRangeWithSingleValue() {
        // 测试单个值
        Range<String> range = RangeTool.toStrRange("value");
        assertNotNull(range);
        assertEquals("value", range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testToStrRangeWithMultipleSeparators() {
        // 测试多个分隔符 - 应该只分割第一个
        Range<String> range = RangeTool.toStrRange("start/end/extra");
        assertNotNull(range);
        assertEquals("start", range.getStart());
        assertEquals("end/extra", range.getEnd());
    }

    @Test
    void testToBigDecimalRange() {
        // 测试 BigDecimal 范围
        Range<BigDecimal> range = RangeTool.toBigDecimalRange("10.5/20.75");
        assertNotNull(range);
        assertEquals(new BigDecimal("10.5"), range.getStart());
        assertEquals(new BigDecimal("20.75"), range.getEnd());
    }

    @Test
    void testToBigDecimalRangeWithNullValues() {
        // 测试 BigDecimal 范围（空值）
        Range<BigDecimal> range1 = RangeTool.toBigDecimalRange("10.5/");
        assertNotNull(range1);
        assertEquals(new BigDecimal("10.5"), range1.getStart());
        assertNull(range1.getEnd());

        Range<BigDecimal> range2 = RangeTool.toBigDecimalRange("/20.75");
        assertNotNull(range2);
        assertNull(range2.getStart());
        assertEquals(new BigDecimal("20.75"), range2.getEnd());
    }

    @Test
    void testToLongRange() {
        // 测试 Long 范围
        Range<Long> range = RangeTool.toLongRange("100/200");
        assertNotNull(range);
        assertEquals(100L, range.getStart());
        assertEquals(200L, range.getEnd());
    }

    @Test
    void testToLongRangeWithNullValues() {
        // 测试 Long 范围（空值）
        Range<Long> range1 = RangeTool.toLongRange("100/");
        assertNotNull(range1);
        assertEquals(100L, range1.getStart());
        assertNull(range1.getEnd());

        Range<Long> range2 = RangeTool.toLongRange("/200");
        assertNotNull(range2);
        assertNull(range2.getStart());
        assertEquals(200L, range2.getEnd());
    }

    @Test
    void testToIntRange() {
        // 测试 Integer 范围
        Range<Integer> range = RangeTool.toIntRange("10/20");
        assertNotNull(range);
        assertEquals(10, range.getStart());
        assertEquals(20, range.getEnd());
    }

    @Test
    void testToIntRangeWithNullValues() {
        // 测试 Integer 范围（空值）
        Range<Integer> range1 = RangeTool.toIntRange("10/");
        assertNotNull(range1);
        assertEquals(10, range1.getStart());
        assertNull(range1.getEnd());

        Range<Integer> range2 = RangeTool.toIntRange("/20");
        assertNotNull(range2);
        assertNull(range2.getStart());
        assertEquals(20, range2.getEnd());
    }

    @Test
    void testToDateRange() {
        // 测试 Date 范围
        String dateStr = "2023-01-01/2023-01-31";
        Range<Date> range = RangeTool.toDateRange(dateStr);
        assertNotNull(range);
        assertNotNull(range.getStart());
        assertNotNull(range.getEnd());
        // 开始日期应该是当天开始
        // 结束日期应该是当天结束
    }

    @Test
    void testToDateRangeWithNullValues() {
        // 测试 Date 范围（空值）
        Range<Date> range1 = RangeTool.toDateRange("2023-01-01/");
        assertNotNull(range1);
        assertNotNull(range1.getStart());
        assertNull(range1.getEnd());

        Range<Date> range2 = RangeTool.toDateRange("/2023-01-31");
        assertNotNull(range2);
        assertNull(range2.getStart());
        assertNotNull(range2.getEnd());
    }

    @Test
    void testToDateRangeWithSingleDate() {
        // 测试单个日期
        Range<Date> range = RangeTool.toDateRange("2023-01-01");
        assertNotNull(range);
        assertNotNull(range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testToDateRangeWithEmptyString() {
        // 测试空字符串
        Range<Date> range = RangeTool.toDateRange("");
        assertNotNull(range);
        assertNull(range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testToIntRangeWithInvalidNumber() {
        // 测试无效数字（应该抛出异常）
        assertThrows(NumberFormatException.class, () -> {
            RangeTool.toIntRange("abc/123");
        });

        assertThrows(NumberFormatException.class, () -> {
            RangeTool.toIntRange("123/abc");
        });
    }

    @Test
    void testToLongRangeWithInvalidNumber() {
        // 测试无效数字（应该抛出异常）
        assertThrows(NumberFormatException.class, () -> {
            RangeTool.toLongRange("abc/123");
        });
    }

    @Test
    void testToBigDecimalRangeWithInvalidNumber() {
        // 测试无效数字（应该抛出异常）
        assertThrows(NumberFormatException.class, () -> {
            RangeTool.toBigDecimalRange("abc/123");
        });
    }

    @Test
    void testToDateRangeWithInvalidDate() {
        // 测试无效日期（应该抛出异常）
        assertThrows(Exception.class, () -> {
            RangeTool.toDateRange("invalid-date/2023-01-01");
        });
    }

    @Test
    void testToStrRangeWithEmptyValues() {
        // 测试空值
        Range<String> range = RangeTool.toStrRange("  /  ");
        assertNotNull(range);
        assertNull(range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testToStrRangeWithOnlySpaces() {
        // 测试只有空格
        Range<String> range = RangeTool.toStrRange("   ");
        assertNotNull(range);
        assertNull(range.getStart());
        assertNull(range.getEnd());
    }

}
