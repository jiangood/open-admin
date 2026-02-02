package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ConvertTool工具类的单元测试
 */
class ConvertToolTest {

    // 测试用的枚举类
    enum TestEnum {
        ONE,
        TWO,
        THREE;
    }

    @Test
    void testConvertNormal() {
        // 测试字符串转整数
        Integer intValue = ConvertTool.convert(Integer.class, "123");
        assertNotNull(intValue);
        assertEquals(123, intValue);

        // 测试整数转字符串
        String strValue = ConvertTool.convert(String.class, 123);
        assertNotNull(strValue);
        assertEquals("123", strValue);

        // 测试布尔值转换
        Boolean boolValue = ConvertTool.convert(Boolean.class, "true");
        assertNotNull(boolValue);
        assertTrue(boolValue);
    }

    @Test
    void testConvertNull() {
        // 测试null值
        Integer result = ConvertTool.convert(Integer.class, null);
        assertNull(result);
    }

    @Test
    void testConvertEnum() {
        // 测试整数转枚举（枚举的ordinal从0开始）
        TestEnum enumValue1 = ConvertTool.convert(TestEnum.class, 0);
        assertNotNull(enumValue1);
        assertEquals(TestEnum.ONE, enumValue1);

        // 测试Long转枚举（这是ConvertTool特别处理的情况）
        TestEnum enumValue2 = ConvertTool.convert(TestEnum.class, 1L);
        assertNotNull(enumValue2);
        assertEquals(TestEnum.TWO, enumValue2);

        // 测试字符串转枚举
        TestEnum enumValue3 = ConvertTool.convert(TestEnum.class, "THREE");
        assertNotNull(enumValue3);
        assertEquals(TestEnum.THREE, enumValue3);
    }

    @Test
    void testConvertList() {
        // 测试List<Integer>转换
        String[] arr = {"1", "2", "3"};
        List<Integer> list = ConvertTool.convert(List.class, arr, Integer.class);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));

        // 测试普通List转换（不指定泛型）
        List<?> list2 = ConvertTool.convert(List.class, arr);
        assertNotNull(list2);
        assertEquals(3, list2.size());
    }
}
