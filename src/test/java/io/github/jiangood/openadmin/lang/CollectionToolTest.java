package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CollectionTool工具类的单元测试
 */
class CollectionToolTest {

    @Test
    void testFindNewElements() {
        // 测试正常情况：有新增元素
        List<Integer> a = List.of(1, 2, 3);
        List<Integer> b = List.of(2, 3, 4, 5);
        List<Integer> result = CollectionTool.findNewElements(a, b);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(4));
        assertTrue(result.contains(5));
        
        // 测试没有新增元素
        List<Integer> c = List.of(1, 2, 3);
        List<Integer> d = List.of(1, 2, 3);
        List<Integer> result2 = CollectionTool.findNewElements(c, d);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        
        // 测试b为空集合
        List<Integer> e = List.of(1, 2, 3);
        List<Integer> f = List.of();
        List<Integer> result3 = CollectionTool.findNewElements(e, f);
        assertNotNull(result3);
        assertTrue(result3.isEmpty());
        
        // 测试a为空集合
        List<Integer> g = List.of();
        List<Integer> h = List.of(1, 2, 3);
        List<Integer> result4 = CollectionTool.findNewElements(g, h);
        assertNotNull(result4);
        assertEquals(3, result4.size());
        assertTrue(result4.contains(1));
        assertTrue(result4.contains(2));
        assertTrue(result4.contains(3));
        
        // 测试使用Set
        Set<String> setA = Set.of("a", "b", "c");
        Set<String> setB = Set.of("b", "c", "d", "e");
        List<String> result5 = CollectionTool.findNewElements(setA, setB);
        assertNotNull(result5);
        assertEquals(2, result5.size());
        assertTrue(result5.contains("d"));
        assertTrue(result5.contains("e"));
    }

    @Test
    void testFindExistingElements() {
        // 测试正常情况：有交集元素
        List<Integer> a = List.of(1, 2, 3, 4);
        List<Integer> b = List.of(3, 4, 5, 6);
        List<Integer> result = CollectionTool.findExistingElements(a, b);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(3));
        assertTrue(result.contains(4));
        
        // 测试没有交集元素
        List<Integer> c = List.of(1, 2, 3);
        List<Integer> d = List.of(4, 5, 6);
        List<Integer> result2 = CollectionTool.findExistingElements(c, d);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        
        // 测试b为空集合
        List<Integer> e = List.of(1, 2, 3);
        List<Integer> f = List.of();
        List<Integer> result3 = CollectionTool.findExistingElements(e, f);
        assertNotNull(result3);
        assertTrue(result3.isEmpty());
        
        // 测试a为空集合
        List<Integer> g = List.of();
        List<Integer> h = List.of(1, 2, 3);
        List<Integer> result4 = CollectionTool.findExistingElements(g, h);
        assertNotNull(result4);
        assertTrue(result4.isEmpty());
        
        // 测试使用Set
        Set<String> setA = Set.of("a", "b", "c", "d");
        Set<String> setB = Set.of("c", "d", "e", "f");
        List<String> result5 = CollectionTool.findExistingElements(setA, setB);
        assertNotNull(result5);
        assertEquals(2, result5.size());
        assertTrue(result5.contains("c"));
        assertTrue(result5.contains("d"));
    }

    @Test
    void testFill() {
        // 测试正常情况：填充元素
        List<String> list = new ArrayList<>();
        CollectionTool.fill(list, "test", 3);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("test", list.get(0));
        assertEquals("test", list.get(1));
        assertEquals("test", list.get(2));
        
        // 测试填充大小为0
        List<String> list2 = new ArrayList<>();
        CollectionTool.fill(list2, "test", 0);
        assertNotNull(list2);
        assertTrue(list2.isEmpty());
        
        // 测试在现有集合上填充
        List<String> list3 = new ArrayList<>(List.of("a", "b"));
        CollectionTool.fill(list3, "c", 2);
        assertNotNull(list3);
        assertEquals(4, list3.size());
        assertEquals("a", list3.get(0));
        assertEquals("b", list3.get(1));
        assertEquals("c", list3.get(2));
        assertEquals("c", list3.get(3));
        
        // 测试使用Set
        Set<Integer> set = new HashSet<>();
        CollectionTool.fill(set, 1, 3);
        assertNotNull(set);
        assertEquals(1, set.size()); // Set会去重
        assertTrue(set.contains(1));
    }

    @Test
    void testFillNull() {
        // 测试正常情况：填充null
        List<String> list = new ArrayList<>();
        CollectionTool.fillNull(list, 3);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertNull(list.get(0));
        assertNull(list.get(1));
        assertNull(list.get(2));
        
        // 测试填充大小为0
        List<String> list2 = new ArrayList<>();
        CollectionTool.fillNull(list2, 0);
        assertNotNull(list2);
        assertTrue(list2.isEmpty());
        
        // 测试在现有集合上填充
        List<String> list3 = new ArrayList<>(List.of("a", "b"));
        CollectionTool.fillNull(list3, 2);
        assertNotNull(list3);
        assertEquals(4, list3.size());
        assertEquals("a", list3.get(0));
        assertEquals("b", list3.get(1));
        assertNull(list3.get(2));
        assertNull(list3.get(3));
    }
}
