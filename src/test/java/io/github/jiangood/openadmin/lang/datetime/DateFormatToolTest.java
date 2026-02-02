package io.github.jiangood.openadmin.lang.datetime;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateFormatToolTest {

    @Test
    void testFormat() {
        Date date = new Date();
        String result = DateFormatTool.format(date);
        assertNotNull(result);
        assertTrue(result.length() > 0);
        // 验证格式是否正确
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testFormatWithNull() {
        assertThrows(NullPointerException.class, () -> {
            DateFormatTool.format(null);
        });
    }

    @Test
    void testFormatDay() {
        Date date = new Date();
        String result = DateFormatTool.formatDay(date);
        assertNotNull(result);
        assertTrue(result.length() > 0);
        // 验证格式是否正确
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testFormatDayWithNull() {
        assertThrows(NullPointerException.class, () -> {
            DateFormatTool.formatDay(null);
        });
    }

    @Test
    void testFormatDayCn() {
        Date date = new Date();
        String result = DateFormatTool.formatDayCn(date);
        assertNotNull(result);
        assertTrue(result.length() > 0);
        // 验证格式是否正确
        assertTrue(result.matches("\\d{4}年\\d+月\\d+日"));
    }

    @Test
    void testFormatDayCnWithNull() {
        assertThrows(NullPointerException.class, () -> {
            DateFormatTool.formatDayCn(null);
        });
    }

}
