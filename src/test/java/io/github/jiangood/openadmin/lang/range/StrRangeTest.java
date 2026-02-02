package io.github.jiangood.openadmin.lang.range;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrRangeTest {

    @Test
    void testConstructorWithNoArgs() {
        StrRange range = new StrRange();
        assertNull(range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testConstructorWithValidString() {
        // 测试有效的字符串范围（使用 "/" 作为分隔符）
        String rangeStr = "a/z";
        StrRange range = new StrRange(rangeStr);
        assertEquals("a", range.getStart());
        assertEquals("z", range.getEnd());
    }

    @Test
    void testConstructorWithEmptyString() {
        // 测试空字符串
        String emptyStr = "";
        StrRange range = new StrRange(emptyStr);
        assertNull(range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testSetters() {
        StrRange range = new StrRange();
        range.setStart("apple");
        range.setEnd("banana");
        assertEquals("apple", range.getStart());
        assertEquals("banana", range.getEnd());
    }

    @Test
    void testInheritedMethods() {
        // 测试 isEmpty
        StrRange emptyRange = new StrRange();
        assertTrue(emptyRange.isEmpty());
        
        // 测试 isSame
        StrRange sameRange = new StrRange();
        sameRange.setStart("test");
        sameRange.setEnd("test");
        assertTrue(sameRange.isSame());
        
        // 测试非相同值
        StrRange differentRange = new StrRange("a/z");
        assertFalse(differentRange.isSame());
        assertFalse(differentRange.isEmpty());
    }

}
