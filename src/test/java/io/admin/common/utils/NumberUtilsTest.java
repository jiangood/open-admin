package io.admin.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class NumberUtilsTest {

    @Test
    public void testTryGetNumber() {
        // 测试整数
        assertEquals(Integer.valueOf(123), NumberUtils.tryGetNumber("123"));
        assertEquals(Integer.valueOf(0), NumberUtils.tryGetNumber("0"));
        assertEquals(Integer.valueOf(-123), NumberUtils.tryGetNumber("-123"));

        // 测试小数
        assertEquals(Float.valueOf(12.34f), NumberUtils.tryGetNumber("12.34"));
        assertEquals(Float.valueOf(0.0f), NumberUtils.tryGetNumber("0.0"));
        assertEquals(Float.valueOf(-12.34f), NumberUtils.tryGetNumber("-12.34"));

        // 测试无效数字
        assertNull(NumberUtils.tryGetNumber("abc"));
        assertNull(NumberUtils.tryGetNumber("12.34.56"));
        assertNull(NumberUtils.tryGetNumber(""));
        assertNull(NumberUtils.tryGetNumber(null));
        assertNull(NumberUtils.tryGetNumber("12a"));
    }
}