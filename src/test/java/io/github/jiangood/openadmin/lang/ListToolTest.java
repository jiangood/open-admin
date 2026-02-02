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
        // 测试正常情况 - 所有元素都是null
        List<Object> allNullList = new ArrayList<>();
        allNullList.add(null);
        allNullList.add(null);
        allNullList.add(null);
        assertTrue(ListTool.isAllNull(allNullList));
        
        // 测试正常情况 - 有非null元素
        List<Object> hasNonNullList = new ArrayList<>();
        hasNonNullList.add(null);
        hasNonNullList.add("test");
        hasNonNullList.add(null);
        assertFalse(ListTool.isAllNull(hasNonNullList));
        
        // 测试空列表
        List<Object> emptyList = new ArrayList<>();
        assertTrue(ListTool.isAllNull(emptyList));
        
        // 测试null列表
        assertThrows(NullPointerException.class, () -> {
            ListTool.isAllNull(null);
        });
    }

    @Test
    void testNullIfEmpty() {
        // 测试正常情况 - 非空列表
        List<String> nonEmptyList = new ArrayList<>();
        nonEmptyList.add("test");
        List<String> result1 = ListTool.nullIfEmpty(nonEmptyList);
        assertEquals(nonEmptyList, result1);
        
        // 测试正常情况 - 空列表
        List<String> emptyList = new ArrayList<>();
        List<String> result2 = ListTool.nullIfEmpty(emptyList);
        assertNull(result2);
        
        // 测试null列表
        List<String> result3 = ListTool.nullIfEmpty(null);
        assertNull(result3);
    }

    @Test
    void testNewFixedSizeList() {
        // 测试正常情况 - 正整数大小
        int size = 5;
        List<Object> result1 = ListTool.newFixedSizeList(size);
        assertNotNull(result1);
        assertEquals(size, result1.size());
        for (Object o : result1) {
            assertNull(o);
        }
        
        // 测试边界情况 - 大小为0
        List<Object> result2 = ListTool.newFixedSizeList(0);
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        
        // 测试边界情况 - 大小为1
        List<Object> result3 = ListTool.newFixedSizeList(1);
        assertNotNull(result3);
        assertEquals(1, result3.size());
        assertNull(result3.get(0));
    }
}
