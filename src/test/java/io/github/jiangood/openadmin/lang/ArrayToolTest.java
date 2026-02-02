package io.github.jiangood.openadmin.lang;

import cn.hutool.core.lang.Pair;
import io.github.jiangood.openadmin.lang.range.IntRange;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ArrayTool工具类的单元测试
 */
class ArrayToolTest {

    @Test
    void testContainsType() {
        // 测试正常情况：包含指定类型
        Object[] arr1 = {"a", "b", 1, "c"};
        assertTrue(ArrayTool.containsType(arr1, String.class));
        assertTrue(ArrayTool.containsType(arr1, Integer.class));
        
        // 测试不包含指定类型
        Object[] arr2 = {"a", "b", "c"};
        assertFalse(ArrayTool.containsType(arr2, Integer.class));
        
        // 测试空数组
        Object[] arr3 = {};
        assertFalse(ArrayTool.containsType(arr3, String.class));
        
        // 测试null数组
        assertFalse(ArrayTool.containsType(null, String.class));
    }

    @Test
    void testIsAllType() {
        // 测试正常情况：所有元素都是指定类型
        Object[] arr1 = {"a", "b", "c"};
        assertTrue(ArrayTool.isAllType(arr1, String.class));
        
        // 测试包含不同类型
        Object[] arr2 = {"a", "b", 1};
        assertFalse(ArrayTool.isAllType(arr2, String.class));
        
        // 测试包含null
        Object[] arr3 = {"a", null, "c"};
        assertFalse(ArrayTool.isAllType(arr3, String.class));
        
        // 测试空数组
        Object[] arr4 = {};
        assertFalse(ArrayTool.isAllType(arr4, String.class));
        
        // 测试null数组
        assertFalse(ArrayTool.isAllType(null, String.class));
    }

    @Test
    void testFindContinuousSame() {
        // 测试正常情况：有连续相同元素
        Object[] arr1 = {"a", "a", "b", "b", "b", "c"};
        List<Pair<Object, IntRange>> result1 = ArrayTool.findContinuousSame(arr1);
        assertNotNull(result1);
        assertEquals(2, result1.size());
        assertEquals("a", result1.get(0).getKey());
        assertEquals(0, result1.get(0).getValue().getStart());
        assertEquals(1, result1.get(0).getValue().getEnd());
        assertEquals("b", result1.get(1).getKey());
        assertEquals(2, result1.get(1).getValue().getStart());
        assertEquals(4, result1.get(1).getValue().getEnd());
        
        // 测试没有连续相同元素
        Object[] arr2 = {"a", "b", "c"};
        List<Pair<Object, IntRange>> result2 = ArrayTool.findContinuousSame(arr2);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        
        // 测试空数组
        Object[] arr3 = {};
        List<Pair<Object, IntRange>> result3 = ArrayTool.findContinuousSame(arr3);
        assertNotNull(result3);
        assertTrue(result3.isEmpty());
        
        // 测试null数组
        List<Pair<Object, IntRange>> result4 = ArrayTool.findContinuousSame(null);
        assertNotNull(result4);
        assertTrue(result4.isEmpty());
    }

    @Test
    void testToStrArr() {
        // 测试正常情况
        List<String> list1 = List.of("a", "b", "c");
        String[] result1 = ArrayTool.toStrArr(list1);
        assertNotNull(result1);
        assertEquals(3, result1.length);
        assertEquals("a", result1[0]);
        assertEquals("b", result1[1]);
        assertEquals("c", result1[2]);
        
        // 测试空列表
        List<String> list2 = List.of();
        String[] result2 = ArrayTool.toStrArr(list2);
        assertNotNull(result2);
        assertEquals(0, result2.length);
        
        // 测试null列表
        String[] result3 = ArrayTool.toStrArr(null);
        assertNotNull(result3);
        assertEquals(0, result3.length);
    }

    @Test
    void testFindIndex() {
        // 测试正常情况：找到满足条件的元素
        Integer[] arr1 = {1, 2, 3, 4, 5};
        int result1 = ArrayTool.findIndex(arr1, n -> n > 3);
        assertEquals(3, result1);
        
        // 测试没有找到满足条件的元素
        Integer[] arr2 = {1, 2, 3};
        int result2 = ArrayTool.findIndex(arr2, n -> n > 5);
        assertEquals(-1, result2);
        
        // 测试空数组
        Integer[] arr3 = {};
        int result3 = ArrayTool.findIndex(arr3, n -> n > 0);
        assertEquals(-1, result3);
    }

    @Test
    void testToList() {
        // 测试正常情况
        String[] arr1 = {"a", "b", "c"};
        List<String> result1 = ArrayTool.toList(arr1);
        assertNotNull(result1);
        assertEquals(3, result1.size());
        assertEquals("a", result1.get(0));
        assertEquals("b", result1.get(1));
        assertEquals("c", result1.get(2));
        
        // 测试空数组
        String[] arr2 = {};
        List<String> result2 = ArrayTool.toList(arr2);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        
        // 测试null数组
        List<String> result3 = ArrayTool.toList(null);
        assertNotNull(result3);
        assertTrue(result3.isEmpty());
    }
}
