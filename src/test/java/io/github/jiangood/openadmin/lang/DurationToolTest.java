package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DurationTool工具类的单元测试
 */
class DurationToolTest {

    @Test
    void testFormatWithCompleteDuration() {
        // 测试包含天、小时、分、秒的完整情况
        Duration duration = Duration.ofDays(1).plusHours(2).plusMinutes(3).plusSeconds(4);
        String formatted = DurationTool.format(duration);
        assertNotNull(formatted);
        assertEquals("1天2小时3分4秒", formatted);
    }

    @Test
    void testFormatWithOnlyDays() {
        // 测试只包含天的情况
        Duration duration = Duration.ofDays(2);
        String formatted = DurationTool.format(duration);
        assertNotNull(formatted);
        assertEquals("2天", formatted);
    }

    @Test
    void testFormatWithOnlyHours() {
        // 测试只包含小时的情况
        Duration duration = Duration.ofHours(3);
        String formatted = DurationTool.format(duration);
        assertNotNull(formatted);
        assertEquals("3小时", formatted);
    }

    @Test
    void testFormatWithOnlyMinutes() {
        // 测试只包含分钟的情况
        Duration duration = Duration.ofMinutes(4);
        String formatted = DurationTool.format(duration);
        assertNotNull(formatted);
        assertEquals("4分", formatted);
    }

    @Test
    void testFormatWithOnlySeconds() {
        // 测试只包含秒的情况
        Duration duration = Duration.ofSeconds(5);
        String formatted = DurationTool.format(duration);
        assertNotNull(formatted);
        assertEquals("5秒", formatted);
    }

    @Test
    void testFormatWithHoursAndMinutes() {
        // 测试包含小时和分钟的情况
        Duration duration = Duration.ofHours(2).plusMinutes(30);
        String formatted = DurationTool.format(duration);
        assertNotNull(formatted);
        assertEquals("2小时30分", formatted);
    }

    @Test
    void testFormatWithMinutesAndSeconds() {
        // 测试包含分钟和秒的情况
        Duration duration = Duration.ofMinutes(5).plusSeconds(30);
        String formatted = DurationTool.format(duration);
        assertNotNull(formatted);
        assertEquals("5分30秒", formatted);
    }

    @Test
    void testFormatWithZeroDuration() {
        // 测试零值的情况
        Duration duration = Duration.ZERO;
        String formatted = DurationTool.format(duration);
        assertNotNull(formatted);
        assertEquals("", formatted);
    }

    @Test
    void testFormatWithNull() {
        // 测试null值的情况
        String formatted = DurationTool.format(null);
        assertNull(formatted);
    }
}
