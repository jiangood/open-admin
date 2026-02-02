package io.github.jiangood.openadmin.lang.range;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class RangeTest {

    @Test
    void testConstructorWithNoArgs() {
        Range<Integer> range = new Range<>();
        assertNull(range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testConstructorWithArgs() {
        Range<Integer> range = new Range<>(1, 10);
        assertEquals(1, range.getStart());
        assertEquals(10, range.getEnd());
    }

    @Test
    void testSetters() {
        Range<Integer> range = new Range<>();
        range.setStart(5);
        range.setEnd(15);
        assertEquals(5, range.getStart());
        assertEquals(15, range.getEnd());
    }

    @Test
    void testIsEmpty() {
        // 空区间
        Range<Integer> emptyRange = new Range<>();
        assertTrue(emptyRange.isEmpty());
        
        // 非空区间
        Range<Integer> nonEmptyRange = new Range<>(1, 10);
        assertFalse(nonEmptyRange.isEmpty());
        
        // 只有开始值
        Range<Integer> startOnlyRange = new Range<>();
        startOnlyRange.setStart(1);
        assertFalse(startOnlyRange.isEmpty());
        
        // 只有结束值
        Range<Integer> endOnlyRange = new Range<>();
        endOnlyRange.setEnd(10);
        assertFalse(endOnlyRange.isEmpty());
    }

    @Test
    void testIsSame() {
        // 相同值
        Range<Integer> sameRange = new Range<>(5, 5);
        assertTrue(sameRange.isSame());
        
        // 不同值
        Range<Integer> differentRange = new Range<>(1, 10);
        assertFalse(differentRange.isSame());
        
        // 空区间
        Range<Integer> emptyRange = new Range<>();
        assertTrue(emptyRange.isSame());
        
        // 只有开始值
        Range<Integer> startOnlyRange = new Range<>();
        startOnlyRange.setStart(5);
        assertFalse(startOnlyRange.isSame());
        
        // 只有结束值
        Range<Integer> endOnlyRange = new Range<>();
        endOnlyRange.setEnd(5);
        assertFalse(endOnlyRange.isSame());
    }

    @Test
    void testWithDifferentTypes() {
        // 测试 String 类型
        Range<String> stringRange = new Range<>("a", "z");
        assertEquals("a", stringRange.getStart());
        assertEquals("z", stringRange.getEnd());
        assertFalse(stringRange.isEmpty());
        assertFalse(stringRange.isSame());
        
        // 测试 Date 类型
        Date now = new Date();
        Range<Date> dateRange = new Range<>(now, now);
        assertEquals(now, dateRange.getStart());
        assertEquals(now, dateRange.getEnd());
        assertFalse(dateRange.isEmpty());
        assertTrue(dateRange.isSame());
    }

}
