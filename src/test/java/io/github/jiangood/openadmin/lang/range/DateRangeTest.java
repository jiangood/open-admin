package io.github.jiangood.openadmin.lang.range;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateRangeTest {

    @Test
    void testConstructorWithValidString() {
        // 测试有效的日期范围字符串（使用 "/" 作为分隔符）
        String dateRangeStr = "2023-01-01/2023-12-31";
        DateRange dateRange = new DateRange(dateRangeStr);
        assertNotNull(dateRange.getStart());
        assertNotNull(dateRange.getEnd());
        assertTrue(dateRange.getStart().before(dateRange.getEnd()));
    }

    @Test
    void testConstructorWithEmptyString() {
        // 测试空字符串
        String emptyStr = "";
        DateRange dateRange = new DateRange(emptyStr);
        assertNull(dateRange.getStart());
        assertNull(dateRange.getEnd());
    }

    @Test
    void testInheritedMethods() {
        // 测试继承的方法（使用 "/" 作为分隔符）
        String dateRangeStr = "2023-01-01/2023-12-31";
        DateRange dateRange = new DateRange(dateRangeStr);
        
        // 测试 isEmpty
        assertFalse(dateRange.isEmpty());
        
        // 测试 isSame
        assertFalse(dateRange.isSame());
        
        // 测试 setters 和 getters
        Date newStart = new Date();
        Date newEnd = new Date();
        dateRange.setStart(newStart);
        dateRange.setEnd(newEnd);
        assertEquals(newStart, dateRange.getStart());
        assertEquals(newEnd, dateRange.getEnd());
    }

}
