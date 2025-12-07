package io.admin.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class IdUtilsTest {

    @Test
    public void testUuidV7() {
        String uuid1 = IdUtils.uuidV7();
        String uuid2 = IdUtils.uuidV7();
        
        assertNotNull(uuid1);
        assertEquals(32, uuid1.length()); // UUID without hyphens should be 32 characters
        assertTrue(uuid1.matches("[0-9a-fA-F]+")); // Should contain only hex characters
        assertNotEquals(uuid1, uuid2); // Two generated UUIDs should be different
    }

    // 注意：nextIdByDb方法依赖于数据库，对于单元测试来说比较复杂，需要Mock
    // 我们可以跳过或创建一个专门的集成测试
    @Test
    public void testUuidV7Format() {
        String uuid = IdUtils.uuidV7();
        // 验证UUID的格式
        assertTrue(uuid.length() == 32);
        assertTrue(uuid.matches("[0-9a-fA-F]+"));
    }
}