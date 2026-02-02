package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AmtTool工具类的单元测试
 */
class AmtToolTest {

    @Test
    void testYuanToFen() {
        // 测试正常数值转换
        BigDecimal yuan = new BigDecimal("123.45");
        int fen = AmtTool.yuanToFen(yuan);
        assertEquals(12345, fen);

        // 测试整数转换
        yuan = new BigDecimal("123");
        fen = AmtTool.yuanToFen(yuan);
        assertEquals(12300, fen);

        // 测试负数转换
        yuan = new BigDecimal("-123.45");
        fen = AmtTool.yuanToFen(yuan);
        assertEquals(-12345, fen);

        // 测试四舍五入
        yuan = new BigDecimal("123.456");
        fen = AmtTool.yuanToFen(yuan);
        assertEquals(12346, fen);
    }

    @Test
    void testFenToYuan() {
        // 测试正常数值转换
        int fen = 12345;
        BigDecimal yuan = AmtTool.fenToYuan(fen);
        assertNotNull(yuan);
        assertEquals(new BigDecimal("123.45"), yuan);

        // 测试整数转换
        fen = 12300;
        yuan = AmtTool.fenToYuan(fen);
        assertNotNull(yuan);
        assertEquals(new BigDecimal("123.00"), yuan);

        // 测试负数转换
        fen = -12345;
        yuan = AmtTool.fenToYuan(fen);
        assertNotNull(yuan);
        assertEquals(new BigDecimal("-123.45"), yuan);

        // 测试零值转换
        fen = 0;
        yuan = AmtTool.fenToYuan(fen);
        assertNotNull(yuan);
        assertEquals(new BigDecimal("0.00"), yuan);
    }

    @Test
    void testRound() {
        // 测试正常数值四舍五入
        BigDecimal amt = new BigDecimal("123.456");
        BigDecimal rounded = AmtTool.round(amt);
        assertNotNull(rounded);
        assertEquals(new BigDecimal("123.46"), rounded);

        // 测试整数四舍五入
        amt = new BigDecimal("123");
        rounded = AmtTool.round(amt);
        assertNotNull(rounded);
        assertEquals(new BigDecimal("123.00"), rounded);

        // 测试负数四舍五入
        amt = new BigDecimal("-123.456");
        rounded = AmtTool.round(amt);
        assertNotNull(rounded);
        assertEquals(new BigDecimal("-123.46"), rounded);
    }

    @Test
    void testToPlainString() {
        // 测试正常数值转换
        BigDecimal n = new BigDecimal("123.45");
        String result = AmtTool.toPlainString(n);
        assertNotNull(result);
        assertEquals("123.45", result);

        // 测试整数转换
        n = new BigDecimal("123");
        result = AmtTool.toPlainString(n);
        assertNotNull(result);
        assertEquals("123", result);

        // 测试包含尾随零的数值转换
        n = new BigDecimal("123.4500");
        result = AmtTool.toPlainString(n);
        assertNotNull(result);
        assertEquals("123.45", result);

        // 测试null值
        result = AmtTool.toPlainString(null);
        assertNotNull(result);
        assertEquals("", result);
    }
}
