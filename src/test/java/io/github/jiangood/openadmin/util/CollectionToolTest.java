package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CollectionTool工具类的单元测试
 */
class CollectionToolTest {

    @Test
    void testFindNewElements() {
        // 测试正常情况 - 有新增元素
        Collection<String> a = List.of("a", "b", "c");
        Collection<String> b = List.of("b", "c", "d", "e");
        List<String> result = CollectionTool.findNewElements(a, b);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("d"));
        assertTrue(result.contains("e"));
        
        // 测试正常情况 - 没有新增元素
        Collection<String> a2 = List.of("a", "b", "c");
        Collection<String> b2 = List.of("a", "b", "c");
        List<String> result2 = CollectionTool.findNewElements(a2, b2);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        
        // 测试正常情况 - a为空
        Collection<String> a3 = List.of();
        Collection<String> b3 = List.of("a", "b", "c");
        List<String> result3 = CollectionTool.findNewElements(a3, b3);
        assertNotNull(result3);
        assertEquals(3, result3.size());
        assertTrue(result3.contains("a"));
        assertTrue(result3.contains("b"));
        assertTrue(result3.contains("c"));
        
        // 测试正常情况 - b为空
        Collection<String> a4 = List.of("a", "b", "c");
        Collection<String> b4 = List.of();
        List<String> result4 = CollectionTool.findNewElements(a4, b4);
        assertNotNull(result4);
        assertTrue(result4.isEmpty());
        
        // 测试边界情况 - 两个集合都为空
        Collection<String> a5 = List.of();
        Collection<String> b5 = List.of();
        List<String> result5 = CollectionTool.findNewElements(a5, b5);
        assertNotNull(result5);
        assertTrue(result5.isEmpty());
    }

    @Test
    void testFindExistingElements() {
        // 测试正常情况 - 有交集
        Collection<String> a = List.of("a", "b", "c");
        Collection<String> b = List.of("b", "c", "d", "e");
        List<String> result = CollectionTool.findExistingElements(a, b);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
        
        // 测试正常情况 - 没有交集
        Collection<String> a2 = List.of("a", "b", "c");
        Collection<String> b2 = List.of("d", "e", "f");
        List<String> result2 = CollectionTool.findExistingElements(a2, b2);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        
        // 测试正常情况 - a为空
        Collection<String> a3 = List.of();
        Collection<String> b3 = List.of("a", "b", "c");
        List<String> result3 = CollectionTool.findExistingElements(a3, b3);
        assertNotNull(result3);
        assertTrue(result3.isEmpty());
        
        // 测试正常情况 - b为空
        Collection<String> a4 = List.of("a", "b", "c");
        Collection<String> b4 = List.of();
        List<String> result4 = CollectionTool.findExistingElements(a4, b4);
        assertNotNull(result4);
        assertTrue(result4.isEmpty());
        
        // 测试边界情况 - 两个集合都为空
        Collection<String> a5 = List.of();
        Collection<String> b5 = List.of();
        List<String> result5 = CollectionTool.findExistingElements(a5, b5);
        assertNotNull(result5);
        assertTrue(result5.isEmpty());
    }

    @Test
    void testFill() {
        // 测试正常情况
        List<String> list = new ArrayList<>();
        CollectionTool.fill(list, "test", 3);
        assertEquals(3, list.size());
        assertEquals("test", list.get(0));
        assertEquals("test", list.get(1));
        assertEquals("test", list.get(2));
        
        // 测试边界情况 - size为0
        List<String> list2 = new ArrayList<>();
        CollectionTool.fill(list2, "test", 0);
        assertTrue(list2.isEmpty());
        
        // 测试边界情况 - size为1
        List<String> list3 = new ArrayList<>();
        CollectionTool.fill(list3, "test", 1);
        assertEquals(1, list3.size());
        assertEquals("test", list3.get(0));
    }

    @Test
    void testFillNull() {
        // 测试正常情况
        List<String> list = new ArrayList<>();
        CollectionTool.fillNull(list, 3);
        assertEquals(3, list.size());
        assertNull(list.get(0));
        assertNull(list.get(1));
        assertNull(list.get(2));
        
        // 测试边界情况 - size为0
        List<String> list2 = new ArrayList<>();
        CollectionTool.fillNull(list2, 0);
        assertTrue(list2.isEmpty());
        
        // 测试边界情况 - size为1
        List<String> list3 = new ArrayList<>();
        CollectionTool.fillNull(list3, 1);
        assertEquals(1, list3.size());
        assertNull(list3.get(0));
    }
}
