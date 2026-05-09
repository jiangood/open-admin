package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssertTest {

    @Test
    void testStateWhenTrue() {
        // 当state为true时，不应该抛出异常
        assertDoesNotThrow(() -> Assert.state(true, 200, "测试成功"));
    }

    @Test
    void testStateWhenFalse() {
        // 当state为false时，应该抛出BizException
        int expectedCode = 400;
        String expectedMsg = "测试失败";
        
        BusinessException exception = assertThrows(BusinessException.class, () ->
            Assert.state(false, expectedCode, expectedMsg)
        );
        
        // 验证异常的消息和代码
        assertEquals(expectedMsg, exception.getMessage());
        assertEquals(expectedCode, exception.getCode());
    }
}
