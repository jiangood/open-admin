package io.github.jiangood.openadmin.lang;

import cn.hutool.core.lang.Pair;
import io.github.jiangood.openadmin.lang.range.IntRange;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ArrayTool工具类的单元测试
 */
class ArrayToolTest {

    @Test
    void testContainsType() {
        // 测试正常情况 - 包含指定类型
        Object[] arr = {"string", 123, true};
        assertTrue(ArrayTool.containsType(arr, String.class));
        assertTrue(ArrayTool.containsType(arr, Integer.class));
        assertTrue(ArrayTool.containsType(arr, Boolean.class));
        // 测试正常情况 - 不包含指定类型
        assertFalse(ArrayTool.containsType(arr, Double.class));
        // 测试空数组
        assertFalse(ArrayTool.containsType(null, String.class));
        assertFalse(ArrayTool.containsType(new Object[0], String.class));
        // 测试包含null元素
        Object[] arrWithNull = {"string", null, true};
        assertTrue(ArrayTool.containsType(arrWithNull, String.class));
        assertFalse(ArrayTool.containsType(arrWithNull, Double.class));
    }

    @Test
    void testIsAllType() {
        // 测试正常情况 - 所有元素都是指定类型
        Object[] arr = {"string1", "string2", "string3"};
        assertTrue(ArrayTool.isAllType(arr, String.class));
        // 测试正常情况 - 不是所有元素都是指定类型
        Object[] mixedArr = {"string", 123, true};
        assertFalse(ArrayTool.isAllType(mixedArr, String.class));
        // 测试空数组
        assertFalse(ArrayTool.isAllType(null, String.class));
        assertFalse(ArrayTool.isAllType(new Object[0], String.class));
        // 测试包含null元素
        Object[] arrWithNull = {"string1", null, "string3"};
        assertFalse(ArrayTool.isAllType(arrWithNull, String.class));
    }

    @Test
    void testFindContinuousSame() {
        // 测试正常情况 - 有连续相同的元素
        Object[] arr = {"a", "a", "b", "b", "b", "c", "d", "d"};
        List<Pair<Object, IntRange>> result = ArrayTool.findContinuousSame(arr);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0).getKey());
        assertEquals(0, result.get(0).getValue().getStart());
        assertEquals(1, result.get(0).getValue().getEnd());
        assertEquals("b", result.get(1).getKey());
        assertEquals(2, result.get(1).getValue().getStart());
        assertEquals(4, result.get(1).getValue().getEnd());
        assertEquals("d", result.get(2).getKey());
        assertEquals(6, result.get(2).getValue().getStart());
        assertEquals(7, result.get(2).getValue().getEnd());
        // 测试空数组
        List<Pair<Object, IntRange>> emptyResult = ArrayTool.findContinuousSame(null);
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());
        emptyResult = ArrayTool.findContinuousSame(new Object[0]);
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());
        // 测试没有连续相同的元素
        Object[] arrNoContinuous = {"a", "b", "c", "d"};
        List<Pair<Object, IntRange>> noContinuousResult = ArrayTool.findContinuousSame(arrNoContinuous);
        assertNotNull(noContinuousResult);
        assertTrue(noContinuousResult.isEmpty());
    }

    @Test
    void testToStrArr() {
        // 测试正常情况
        List<String> list = new ArrayList<>();
        list.add("string1");
        list.add("string2");
        list.add("string3");
        String[] result = ArrayTool.toStrArr(list);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("string1", result[0]);
        assertEquals("string2", result[1]);
        assertEquals("string3", result[2]);
        // 测试空列表
        String[] emptyResult = ArrayTool.toStrArr(null);
        assertNotNull(emptyResult);
        assertEquals(0, emptyResult.length);
        emptyResult = ArrayTool.toStrArr(new ArrayList<>());
        assertNotNull(emptyResult);
        assertEquals(0, emptyResult.length);
    }

    @Test
    void testFindIndex() {
        // 测试正常情况 - 找到满足条件的元素
        Integer[] arr = {1, 2, 3, 4, 5};
        int index = ArrayTool.findIndex(arr, n -> n > 3);
        assertEquals(3, index);
        // 测试正常情况 - 没有找到满足条件的元素
        int notFoundIndex = ArrayTool.findIndex(arr, n -> n > 10);
        assertEquals(-1, notFoundIndex);
        // 测试空数组
        int emptyIndex = ArrayTool.findIndex(new Integer[0], n -> n > 3);
        assertEquals(-1, emptyIndex);
    }

    @Test
    void testToList() {
        // 测试正常情况
        Integer[] arr = {1, 2, 3, 4, 5};
        List<Integer> result = ArrayTool.toList(arr);
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));
        assertEquals(4, result.get(3));
        assertEquals(5, result.get(4));
        // 测试空数组
        List<Integer> emptyResult = ArrayTool.toList(null);
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());
        emptyResult = ArrayTool.toList(new Integer[0]);
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());
    }
}
