package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * MapTool工具类的单元测试
 */
class MapToolTest {

    @Test
    void testRemoveNullOrEmptyValue() {
        // 测试正常情况：包含null和空字符串值
        Map<String, Object> map1 = new HashMap<>();
        map1.put("name", "test");
        map1.put("age", null);
        map1.put("email", "");
        map1.put("phone", "123456");
        
        Map<String, Object> result1 = MapTool.removeNullOrEmptyValue(map1);
        assertNotNull(result1);
        assertEquals(2, result1.size());
        assertTrue(result1.containsKey("name"));
        assertTrue(result1.containsKey("phone"));
        assertFalse(result1.containsKey("age"));
        assertFalse(result1.containsKey("email"));
        
        // 测试空Map
        Map<String, Object> map2 = new HashMap<>();
        Map<String, Object> result2 = MapTool.removeNullOrEmptyValue(map2);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        
        // 测试所有值都是null或空字符串
        Map<String, Object> map3 = new HashMap<>();
        map3.put("name", null);
        map3.put("age", "");
        
        Map<String, Object> result3 = MapTool.removeNullOrEmptyValue(map3);
        assertNotNull(result3);
        assertTrue(result3.isEmpty());
        
        // 测试没有null或空字符串值
        Map<String, Object> map4 = new HashMap<>();
        map4.put("name", "test");
        map4.put("age", 18);
        
        Map<String, Object> result4 = MapTool.removeNullOrEmptyValue(map4);
        assertNotNull(result4);
        assertEquals(2, result4.size());
        assertEquals("test", result4.get("name"));
        assertEquals(18, result4.get("age"));
        
        // 测试包含数字0（不应该被移除）
        Map<String, Object> map5 = new HashMap<>();
        map5.put("name", "test");
        map5.put("age", 0);
        map5.put("score", 0.0);
        
        Map<String, Object> result5 = MapTool.removeNullOrEmptyValue(map5);
        assertNotNull(result5);
        assertEquals(3, result5.size());
        assertEquals(0, result5.get("age"));
        assertEquals(0.0, result5.get("score"));
    }

    @Test
    void testPutIfValue() {
        // 测试正常情况：值不为null
        Map<String, Object> map1 = new HashMap<>();
        MapTool.putIfValue(map1, "name", "test");
        MapTool.putIfValue(map1, "age", 18);
        
        assertNotNull(map1);
        assertEquals(2, map1.size());
        assertEquals("test", map1.get("name"));
        assertEquals(18, map1.get("age"));
        
        // 测试值为null（不应该添加）
        Map<String, Object> map2 = new HashMap<>();
        MapTool.putIfValue(map2, "name", "test");
        MapTool.putIfValue(map2, "age", null);
        
        assertNotNull(map2);
        assertEquals(1, map2.size());
        assertEquals("test", map2.get("name"));
        assertFalse(map2.containsKey("age"));
        
        // 测试map为null（不应该抛出异常）
        try {
            MapTool.putIfValue(null, "name", "test");
            // 应该执行到这里，不抛出异常
            assertTrue(true);
        } catch (Exception e) {
            fail("map为null时不应该抛出异常");
        }
        
        // 测试键为null，值不为null（应该添加）
        Map<String, Object> map3 = new HashMap<>();
        MapTool.putIfValue(map3, null, "test");
        
        assertNotNull(map3);
        assertEquals(1, map3.size());
        assertEquals("test", map3.get(null));
    }

    @Test
    void testUnderlineKeys() {
        // 测试正常情况：包含驼峰命名的键
        Map<String, Object> map1 = new HashMap<>();
        map1.put("userName", "test");
        map1.put("userAge", 18);
        map1.put("email", "test@example.com"); // 不需要转换
        
        MapTool.underlineKeys(map1);
        
        assertNotNull(map1);
        assertEquals(3, map1.size());
        assertTrue(map1.containsKey("user_name"));
        assertTrue(map1.containsKey("user_age"));
        assertTrue(map1.containsKey("email"));
        assertEquals("test", map1.get("user_name"));
        assertEquals(18, map1.get("user_age"));
        assertEquals("test@example.com", map1.get("email"));
        assertFalse(map1.containsKey("userName"));
        assertFalse(map1.containsKey("userAge"));
        
        // 测试空Map
        Map<String, Object> map2 = new HashMap<>();
        MapTool.underlineKeys(map2);
        assertNotNull(map2);
        assertTrue(map2.isEmpty());
        
        // 测试所有键都不需要转换
        Map<String, Object> map3 = new HashMap<>();
        map3.put("name", "test");
        map3.put("age", 18);
        map3.put("email", "test@example.com");
        
        MapTool.underlineKeys(map3);
        
        assertNotNull(map3);
        assertEquals(3, map3.size());
        assertTrue(map3.containsKey("name"));
        assertTrue(map3.containsKey("age"));
        assertTrue(map3.containsKey("email"));
        assertEquals("test", map3.get("name"));
        assertEquals(18, map3.get("age"));
        assertEquals("test@example.com", map3.get("email"));
        
        // 测试复杂的驼峰命名
        Map<String, Object> map4 = new HashMap<>();
        map4.put("firstName", "John");
        map4.put("lastName", "Doe");
        map4.put("userAddressStreet", "Main St");
        
        MapTool.underlineKeys(map4);
        
        assertNotNull(map4);
        assertEquals(3, map4.size());
        assertTrue(map4.containsKey("first_name"));
        assertTrue(map4.containsKey("last_name"));
        assertTrue(map4.containsKey("user_address_street"));
        assertEquals("John", map4.get("first_name"));
        assertEquals("Doe", map4.get("last_name"));
        assertEquals("Main St", map4.get("user_address_street"));
    }
}
