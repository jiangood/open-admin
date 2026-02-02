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
        // 测试正常情况 - 有null和空值
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", null);
        map.put("key3", "");
        map.put("key4", "value4");
        
        Map<String, Object> result = MapTool.removeNullOrEmptyValue(map);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("key1"));
        assertTrue(result.containsKey("key4"));
        assertFalse(result.containsKey("key2"));
        assertFalse(result.containsKey("key3"));
        
        // 测试空map
        Map<String, Object> emptyMap = new HashMap<>();
        Map<String, Object> result2 = MapTool.removeNullOrEmptyValue(emptyMap);
        assertTrue(result2.isEmpty());
        
        // 测试所有值都是null或空
        Map<String, Object> allNullMap = new HashMap<>();
        allNullMap.put("key1", null);
        allNullMap.put("key2", "");
        Map<String, Object> result3 = MapTool.removeNullOrEmptyValue(allNullMap);
        assertTrue(result3.isEmpty());
    }

    @Test
    void testPutIfValue() {
        // 测试正常情况 - 值不为null
        Map<String, Object> map = new HashMap<>();
        MapTool.putIfValue(map, "key1", "value1");
        assertEquals(1, map.size());
        assertEquals("value1", map.get("key1"));
        
        // 测试正常情况 - 值为null
        MapTool.putIfValue(map, "key2", null);
        assertEquals(1, map.size());
        assertFalse(map.containsKey("key2"));
        
        // 测试null map
        MapTool.putIfValue(null, "key1", "value1");
        // 应该不会抛出异常
    }

    @Test
    void testUnderlineKeys() {
        // 测试正常情况 - 有驼峰命名的键
        Map<String, Object> map = new HashMap<>();
        map.put("userName", "test");
        map.put("userAge", 20);
        map.put("address", "test address");
        
        MapTool.underlineKeys(map);
        assertEquals(3, map.size());
        assertTrue(map.containsKey("user_name"));
        assertTrue(map.containsKey("user_age"));
        assertTrue(map.containsKey("address"));
        assertEquals("test", map.get("user_name"));
        assertEquals(20, map.get("user_age"));
        assertEquals("test address", map.get("address"));
        
        // 测试空map
        Map<String, Object> emptyMap = new HashMap<>();
        MapTool.underlineKeys(emptyMap);
        assertTrue(emptyMap.isEmpty());
        
        // 测试所有键都是下划线格式
        Map<String, Object> allUnderlineMap = new HashMap<>();
        allUnderlineMap.put("user_name", "test");
        allUnderlineMap.put("user_age", 20);
        MapTool.underlineKeys(allUnderlineMap);
        assertEquals(2, allUnderlineMap.size());
        assertTrue(allUnderlineMap.containsKey("user_name"));
        assertTrue(allUnderlineMap.containsKey("user_age"));
    }
}
