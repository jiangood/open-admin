package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * IdTool工具类的单元测试
 */
class IdToolTest {

    @Test
    void testUuidV7() {
        // 测试生成UUID v7
        String uuid1 = IdTool.uuidV7();
        assertNotNull(uuid1);
        assertFalse(uuid1.isEmpty());
        
        // 测试生成的UUID不包含连字符
        assertFalse(uuid1.contains("-"));
        
        // 测试UUID长度（UUID v7应该是32位，不含连字符）
        assertEquals(32, uuid1.length());
        
        // 测试多次生成的UUID不同
        String uuid2 = IdTool.uuidV7();
        String uuid3 = IdTool.uuidV7();
        
        assertNotNull(uuid2);
        assertNotNull(uuid3);
        assertNotEquals(uuid1, uuid2, "多次生成的UUID应该不同");
        assertNotEquals(uuid2, uuid3, "多次生成的UUID应该不同");
        assertNotEquals(uuid1, uuid3, "多次生成的UUID应该不同");
        
        // 测试UUID格式（应该是十六进制字符）
        assertTrue(uuid1.matches("[0-9a-fA-F]{32}"), "UUID应该由32个十六进制字符组成");
        assertTrue(uuid2.matches("[0-9a-fA-F]{32}"), "UUID应该由32个十六进制字符组成");
        assertTrue(uuid3.matches("[0-9a-fA-F]{32}"), "UUID应该由32个十六进制字符组成");
    }

    @Test
    void testUuidV7Performance() {
        // 测试UUID生成的性能和一致性
        int count = 100;
        String[] uuids = new String[count];
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            uuids[i] = IdTool.uuidV7();
        }
        long endTime = System.currentTimeMillis();
        
        // 验证所有UUID都不为null
        for (int i = 0; i < count; i++) {
            assertNotNull(uuids[i], "生成的UUID不应为null");
            assertFalse(uuids[i].isEmpty(), "生成的UUID不应为空");
            assertEquals(32, uuids[i].length(), "UUID长度应为32位");
        }
        
        // 验证所有UUID都不同
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                assertNotEquals(uuids[i], uuids[j], "UUID应该是唯一的");
            }
        }
        
        // 性能检查：生成100个UUID应该在合理时间内完成（小于1秒）
        long duration = endTime - startTime;
        assertTrue(duration < 1000, "生成100个UUID应该在1秒内完成，实际耗时: " + duration + "ms");
    }
}
