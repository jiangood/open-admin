package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssertUtilTest {

    @Test
    void testStateWhenTrue() {
        // 当state为true时，不应该抛出异常
        assertDoesNotThrow(() -> AssertUtil.state(true, 200, "测试成功"));
    }

    @Test
    void testStateWhenFalse() {
        // 当state为false时，应该抛出BizException
        int expectedCode = 400;
        String expectedMsg = "测试失败";
        
        BusinessException exception = assertThrows(BusinessException.class, () ->
            AssertUtil.state(false, expectedCode, expectedMsg)
        );
        
        // 验证异常的消息和代码
        assertEquals(expectedMsg, exception.getMessage());
        assertEquals(expectedCode, exception.getCode());
    }
}
