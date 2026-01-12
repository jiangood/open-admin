package io.github.jiangood.as.common.tools;

import io.github.jiangood.as.common.tools.range.Range;
import io.github.jiangood.as.common.tools.range.RangeTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangeToolTest {

    @Test
    void toRange() {
        Range range = RangeTool.toStrRange("2022-10-11/2022-10-12");
        assertEquals("2022-10-11", range.getBegin());
        assertEquals("2022-10-12", range.getEnd());
        assertFalse(range.isEmpty());

        range = RangeTool.toStrRange("");
        assertTrue(range.isEmpty());
        assertNull(range.getBegin());
        assertNull(range.getEnd());

        range = RangeTool.toStrRange("2022-10-11");
        assertEquals("2022-10-11", range.getBegin());
        assertNull(range.getEnd());
        assertFalse(range.isEmpty());

        range = RangeTool.toStrRange("/2022-10-12");
        assertNull(range.getBegin());
        assertEquals("2022-10-12", range.getEnd());
        assertFalse(range.isEmpty());

        range = RangeTool.toStrRange("2022-10-11/");
        assertEquals("2022-10-11", range.getBegin());
        assertNull(range.getEnd());
        assertFalse(range.isEmpty());


        assertThrows(Exception.class, () -> {
            RangeTool.toStrRange("2022-10-11/2022-10-12/2022-10-13");
        });

    }
}
