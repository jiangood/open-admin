package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ListTool工具类的单元测试
 */
class ListToolTest {

    @Test
    void testIsAllNull() {
        // 测试正常情况：所有元素都是null
        List<String> list1 = new ArrayList<>();
        list1.add(null);
        list1.add(null);
        list1.add(null);
        assertTrue(ListTool.isAllNull(list1));
        
        // 测试包含非null元素
        List<String> list2 = new ArrayList<>();
        list2.add(null);
        list2.add("test");
        list2.add(null);
        assertFalse(ListTool.isAllNull(list2));
        
        // 测试空列表
        List<String> list3 = List.of();
        assertTrue(ListTool.isAllNull(list3));
        
        // 测试只有一个null元素
        List<String> list4 = new ArrayList<>();
        list4.add(null);
        assertTrue(ListTool.isAllNull(list4));
        
        // 测试只有一个非null元素
        List<String> list5 = List.of("test");
        assertFalse(ListTool.isAllNull(list5));
        
        // 测试混合类型
        List<Object> list6 = new ArrayList<>();
        list6.add(null);
        list6.add(1);
        list6.add(null);
        assertFalse(ListTool.isAllNull(list6));
    }

    @Test
    void testNullIfEmpty() {
        // 测试正常情况：非空列表
        List<String> list1 = List.of("a", "b", "c");
        List<String> result1 = ListTool.nullIfEmpty(list1);
        assertNotNull(result1);
        assertEquals(list1, result1); // 应该返回原列表
        
        // 测试空列表
        List<String> list2 = List.of();
        assertNull(ListTool.nullIfEmpty(list2));
        
        // 测试null列表
        assertNull(ListTool.nullIfEmpty(null));
        
        // 测试只有一个元素的列表
        List<String> list3 = List.of("test");
        List<String> result3 = ListTool.nullIfEmpty(list3);
        assertNotNull(result3);
        assertEquals(list3, result3);
    }

    @Test
    void testNewFixedSizeList() {
        // 测试正常情况：指定大小的列表
        int size = 3;
        List<String> list1 = ListTool.newFixedSizeList(size);
        assertNotNull(list1);
        assertEquals(size, list1.size());
        assertNull(list1.get(0));
        assertNull(list1.get(1));
        assertNull(list1.get(2));
        
        // 测试大小为0
        List<String> list2 = ListTool.newFixedSizeList(0);
        assertNotNull(list2);
        assertTrue(list2.isEmpty());
        
        // 测试大小为1
        List<String> list3 = ListTool.newFixedSizeList(1);
        assertNotNull(list3);
        assertEquals(1, list3.size());
        assertNull(list3.get(0));
        
        // 测试较大的大小
        int largeSize = 10;
        List<String> list4 = ListTool.newFixedSizeList(largeSize);
        assertNotNull(list4);
        assertEquals(largeSize, list4.size());
        for (int i = 0; i < largeSize; i++) {
            assertNull(list4.get(i));
        }
    }
}
