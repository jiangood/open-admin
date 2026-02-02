package io.github.jiangood.openadmin.lang;

import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GoogleTool工具类的单元测试
 */
class GoogleToolTest {

    @Test
    void testNewMultimap() {
        // 测试创建Multimap
        Multimap<String, Integer> multimap = GoogleTool.newMultimap();
        assertNotNull(multimap);

        // 测试添加元素
        multimap.put("key1", 1);
        multimap.put("key1", 2);
        multimap.put("key2", 3);

        // 测试获取元素
        assertEquals(2, multimap.get("key1").size());
        assertEquals(1, multimap.get("key2").size());

        // 测试移除元素
        multimap.remove("key1", 1);
        assertEquals(1, multimap.get("key1").size());

        // 测试清空
        multimap.clear();
        assertEquals(0, multimap.size());
    }
}
