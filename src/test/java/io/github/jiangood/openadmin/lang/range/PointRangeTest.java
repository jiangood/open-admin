package io.github.jiangood.openadmin.lang.range;

import io.github.jiangood.openadmin.lang.dto.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointRangeTest {

    @Test
    void testConstructorWithNoArgs() {
        PointRange range = new PointRange();
        assertNull(range.getStart());
        assertNull(range.getEnd());
    }

    @Test
    void testConstructorWithArgs() {
        Point startPoint = new Point(1, 2);
        Point endPoint = new Point(3, 4);
        PointRange range = new PointRange(startPoint, endPoint);
        assertEquals(startPoint, range.getStart());
        assertEquals(endPoint, range.getEnd());
    }

    @Test
    void testSetters() {
        PointRange range = new PointRange();
        Point newStart = new Point(5, 6);
        Point newEnd = new Point(7, 8);
        range.setStart(newStart);
        range.setEnd(newEnd);
        assertEquals(newStart, range.getStart());
        assertEquals(newEnd, range.getEnd());
    }

    @Test
    void testInheritedMethods() {
        // 测试 isEmpty
        PointRange emptyRange = new PointRange();
        assertTrue(emptyRange.isEmpty());
        
        // 测试 isSame
        Point samePoint = new Point(1, 2);
        PointRange sameRange = new PointRange(samePoint, samePoint);
        assertTrue(sameRange.isSame());
        
        // 测试非相同值
        Point startPoint = new Point(1, 2);
        Point endPoint = new Point(3, 4);
        PointRange differentRange = new PointRange(startPoint, endPoint);
        assertFalse(differentRange.isSame());
        assertFalse(differentRange.isEmpty());
    }

}
