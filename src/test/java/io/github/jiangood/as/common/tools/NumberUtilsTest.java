package io.github.jiangood.as.common.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class NumberUtilsTest {

    @Test
    public void testTryGetNumber() {
        // 测试整数
        assertEquals(Integer.valueOf(123), NumberTool.tryGetNumber("123"));
        assertEquals(Integer.valueOf(0), NumberTool.tryGetNumber("0"));
        assertEquals(Integer.valueOf(-123), NumberTool.tryGetNumber("-123"));

        // 测试小数
        assertEquals(Float.valueOf(12.34f), NumberTool.tryGetNumber("12.34"));
        assertEquals(Float.valueOf(0.0f), NumberTool.tryGetNumber("0.0"));
        assertEquals(Float.valueOf(-12.34f), NumberTool.tryGetNumber("-12.34"));

        // 测试无效数字
        assertNull(NumberTool.tryGetNumber("abc"));
        assertNull(NumberTool.tryGetNumber("12.34.56"));
        assertNull(NumberTool.tryGetNumber(""));
        assertNull(NumberTool.tryGetNumber(null));
        assertNull(NumberTool.tryGetNumber("12a"));
    }
}
