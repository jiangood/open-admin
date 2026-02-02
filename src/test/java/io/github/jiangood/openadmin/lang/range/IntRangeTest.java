package io.github.jiangood.openadmin.lang.range;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntRangeTest {

    @Test
    void testConstructorWithNoArgs() {
        IntRange range = new IntRange();
        assertNull(range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testConstructorWithArgs() {
        IntRange range = new IntRange(10, 20);
        assertEquals(10, range.getStart());
        assertEquals(20, range.getEnd());
    }

    @Test
    void testSetters() {
        IntRange range = new IntRange();
        range.setStart(5);
        range.setEnd(15);
        assertEquals(5, range.getStart());
        assertEquals(15, range.getEnd());
    }

    @Test
    void testInheritedMethods() {
        // 测试 isEmpty
        IntRange emptyRange = new IntRange();
        assertTrue(emptyRange.isEmpty());
        
        // 测试 isSame
        IntRange sameRange = new IntRange(10, 10);
        assertTrue(sameRange.isSame());
        
        // 测试非相同值
        IntRange differentRange = new IntRange(10, 20);
        assertFalse(differentRange.isSame());
        assertFalse(differentRange.isEmpty());
    }

}
