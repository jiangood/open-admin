package io.github.jiangood.openadmin.lang.datetime;

import io.github.jiangood.openadmin.lang.range.Range;
import org.junit.jupiter.api.Test;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DateTool工具类的单元测试
 */
class DateToolTest {

    @Test
    void testIsIsoDateRange() {
        // 测试正常情况：ISO格式的日期范围（长度21，4个连字符）
        assertTrue(DateTool.isIsoDateRange("2023-01-01-00:00-2023"));
        
        // 测试异常情况：长度不对
        assertFalse(DateTool.isIsoDateRange("2023-01-01T00:00:00"));
        
        // 测试异常情况：包含斜杠
        assertFalse(DateTool.isIsoDateRange("2023-01-01/2023-01-31"));
        
        // 测试异常情况：连字符数量不对
        assertFalse(DateTool.isIsoDateRange("2023-01-01T00:00:00"));
    }

    @Test
    void testIsBetween() {
        // 测试正常情况：日期在范围内
        assertTrue(DateTool.isBetween("2023-01-15", "2023-01-01", "2023-01-31"));
        
        // 测试正常情况：日期在边界上
        assertTrue(DateTool.isBetween("2023-01-01", "2023-01-01", "2023-01-31"));
        assertTrue(DateTool.isBetween("2023-01-31", "2023-01-01", "2023-01-31"));
        
        // 测试异常情况：日期在范围外
        assertFalse(DateTool.isBetween("2023-02-01", "2023-01-01", "2023-01-31"));
        
        // 测试异常情况：长度不一致
        assertThrows(IllegalStateException.class, () -> {
            DateTool.isBetween("2023-01-15", "2023-01", "2023-01-31");
        });
    }

    @Test
    void testAllDaysOfMonth() {
        // 测试正常情况：获取某个月的所有日期
        Date date = cn.hutool.core.date.DateUtil.parseDate("2023-01-01");
        List<String> days = DateTool.allDaysOfMonth(date);
        assertNotNull(days);
        assertEquals(31, days.size());
        assertEquals("2023-01-01", days.get(0));
        assertEquals("2023-01-31", days.get(30));
    }

    @Test
    void testCountWithRange() {
        // 测试正常情况：使用Range对象计算天数
        Range<String> range = new Range<>("2023-01-01", "2023-01-03");
        assertEquals(3, DateTool.count(range));
    }

    @Test
    void testCountWithStrings() {
        // 测试年份范围
        assertEquals(3, DateTool.count("2021", "2023"));
        
        // 测试月份范围
        assertEquals(3, DateTool.count("2023-01", "2023-03"));
        
        // 测试日期范围
        assertEquals(3, DateTool.count("2023-01-01", "2023-01-03"));
        
        // 测试季度范围
        assertEquals(3, DateTool.count("2023-Q1", "2023-Q3"));
        
        // 测试相同日期
        assertEquals(1, DateTool.count("2023-01-01", "2023-01-01"));
        
        // 测试异常情况：开始时间大于结束时间
        assertThrows(IllegalStateException.class, () -> {
            DateTool.count("2023-01-03", "2023-01-01");
        });
    }

    @Test
    void testDaysWithLocalDateTime() {
        // 测试正常情况：计算两个LocalDateTime之间的天数差
        LocalDateTime a = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime b = LocalDateTime.of(2023, 1, 3, 0, 0, 0);
        assertEquals(2, DateTool.days(a, b));
    }

    @Test
    void testDaysWithDate() {
        // 测试正常情况：计算两个Date之间的天数差
        Date a = cn.hutool.core.date.DateUtil.parseDate("2023-01-01");
        Date b = cn.hutool.core.date.DateUtil.parseDate("2023-01-03");
        assertEquals(2, DateTool.days(a, b));
    }

    @Test
    void testGetYearByYearMonthStr() {
        // 测试正常情况：从年月字符串中提取年份
        assertEquals(2023, DateTool.getYearByYearMonthStr("2023-01"));
    }

    @Test
    void testGetYear() {
        // 测试正常情况：获取日期的年份
        Date date = cn.hutool.core.date.DateUtil.parseDate("2023-01-01");
        assertEquals(2023, DateTool.getYear(date));
    }

    @Test
    void testGetMonth() {
        // 测试正常情况：获取日期的月份（从1开始）
        Date date = cn.hutool.core.date.DateUtil.parseDate("2023-01-01");
        assertEquals(1, DateTool.getMonth(date));
        
        Date date2 = cn.hutool.core.date.DateUtil.parseDate("2023-12-01");
        assertEquals(12, DateTool.getMonth(date2));
    }

    @Test
    void testGetLastMonthOfTheSameYear() throws ParseException {
        // 测试正常情况：获取同年的上个月
        assertEquals("2023-01", DateTool.getLastMonthOfTheSameYear("2023-02"));
        
        // 测试异常情况：跨年份
        assertNull(DateTool.getLastMonthOfTheSameYear("2023-01"));
    }
}
