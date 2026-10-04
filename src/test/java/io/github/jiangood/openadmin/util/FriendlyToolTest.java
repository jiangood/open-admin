package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FriendlyTool工具类的单元测试
 */
class FriendlyToolTest {

    @Test
    void testGetPercentage() {
        // 测试正常百分比转换
        String percentage = FriendlyTool.getPercentage(0.85, 1);
        assertNotNull(percentage);
        assertEquals("85.0%", percentage);

        percentage = FriendlyTool.getPercentage(0.5, 2);
        assertNotNull(percentage);
        assertEquals("50.00%", percentage);

        percentage = FriendlyTool.getPercentage(1.0, 0);
        assertNotNull(percentage);
        assertEquals("100%", percentage);

        percentage = FriendlyTool.getPercentage(0.0, 1);
        assertNotNull(percentage);
        assertEquals("0.0%", percentage);
    }

    @Test
    void testGetBoolean() {
        // 测试布尔值转换为中文
        assertEquals("是", FriendlyTool.getBoolean(true));
        assertEquals("否", FriendlyTool.getBoolean(false));
    }

    @Test
    void testGetIconBoolean() {
        // 测试布尔值转换为图标
        assertEquals("✓", FriendlyTool.getIconBoolean(true));
        assertEquals("✗", FriendlyTool.getIconBoolean(false));
    }

    @Test
    void testGetDataSize() {
        // 测试数据大小转换
        String dataSize = FriendlyTool.getDataSize(1024);
        assertNotNull(dataSize);
        assertTrue(dataSize.contains("1"));

        dataSize = FriendlyTool.getDataSize(1024 * 1024);
        assertNotNull(dataSize);
        assertTrue(dataSize.contains("1"));

        dataSize = FriendlyTool.getDataSize(0);
        assertNotNull(dataSize);
    }

    @Test
    void testGetPastTime() {
        // 测试过去时间计算
        // 测试null值
        assertThrows(IllegalArgumentException.class, () -> {
            FriendlyTool.getPastTime(null);
        });

        // 测试当前时间
        LocalDateTime now = LocalDateTime.now();
        String pastTime = FriendlyTool.getPastTime(now);
        assertNotNull(pastTime);

        // 测试1分钟前
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        pastTime = FriendlyTool.getPastTime(oneMinuteAgo);
        assertNotNull(pastTime);

        // 测试1小时前
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        pastTime = FriendlyTool.getPastTime(oneHourAgo);
        assertNotNull(pastTime);

        // 测试1天前
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        pastTime = FriendlyTool.getPastTime(oneDayAgo);
        assertNotNull(pastTime);
    }

    @Test
    void testGetTimeDiffWithDates() {
        // 测试时间差计算
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusSeconds(1);
        String timeDiff = FriendlyTool.getTimeDiff(startTime, endTime);
        assertNotNull(timeDiff);

        // 测试开始时间大于结束时间
        timeDiff = FriendlyTool.getTimeDiff(endTime, startTime);
        assertNotNull(timeDiff);
        assertEquals("时间错误", timeDiff);
    }

    @Test
    void testGetTimeDiffWithLongs() {
        // 测试时间差计算（使用时间戳）
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 1000;
        String timeDiff = FriendlyTool.getTimeDiff(startTime, endTime);
        assertNotNull(timeDiff);

        // 测试开始时间大于结束时间
        timeDiff = FriendlyTool.getTimeDiff(endTime, startTime);
        assertNotNull(timeDiff);
        assertEquals("时间错误", timeDiff);
    }
}
