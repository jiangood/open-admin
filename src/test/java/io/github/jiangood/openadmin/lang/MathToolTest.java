package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * MathTool工具类的单元测试
 */
class MathToolTest {

    @Test
    void testPercentStrWithDefaultDecimalPlaces() {
        // 测试默认小数位数（2位）
        assertEquals("20%", MathTool.percentStr(1, 5));
        assertEquals("50%", MathTool.percentStr(2, 4));
        assertEquals("100%", MathTool.percentStr(5, 5));
        assertEquals("0%", MathTool.percentStr(0, 5));
    }

    @Test
    void testPercentStrWithCustomDecimalPlaces() {
        // 测试自定义小数位数
        assertEquals("20%", MathTool.percentStr(1, 5, 0));
        assertEquals("20%", MathTool.percentStr(1, 5, 1));
        assertEquals("20%", MathTool.percentStr(1, 5, 3));
        assertEquals("33.33%", MathTool.percentStr(1, 3, 2));
        assertEquals("33.3333%", MathTool.percentStr(1, 3, 4));
    }

    @Test
    void testPercentStrWithDifferentNumberTypes() {
        // 测试不同类型的数字输入
        assertEquals("20%", MathTool.percentStr(1L, 5L));
        assertEquals("20%", MathTool.percentStr(1.0, 5.0));
        assertEquals("20%", MathTool.percentStr((short) 1, (short) 5));
        assertEquals("20%", MathTool.percentStr((byte) 1, (byte) 5));
    }

    @Test
    void testPercentStrWithLargeNumbers() {
        // 测试大数字
        assertEquals("50%", MathTool.percentStr(1000000, 2000000));
        assertEquals("0.01%", MathTool.percentStr(1, 10000, 2));
    }
}
