package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void testDefaultConstructor() {
        BusinessException exception = new BusinessException();
        assertNull(exception.getMessage());
        assertEquals(0, exception.getCode());
    }

    @Test
    void testConstructorWithMessage() {
        String expectedMsg = "测试异常消息";
        BusinessException exception = new BusinessException(expectedMsg);
        assertEquals(expectedMsg, exception.getMessage());
        assertEquals(0, exception.getCode());
    }

    @Test
    void testConstructorWithPrefixMessageAndThrowable() {
        String prefixMsg = "前缀消息";
        Throwable cause = new RuntimeException("原因异常");
        BusinessException exception = new BusinessException(prefixMsg, cause);
        assertEquals(prefixMsg + ": " + cause.getMessage(), exception.getMessage());
        assertEquals(0, exception.getCode());
    }

    @Test
    void testConstructorWithCodeAndMessage() {
        int expectedCode = 500;
        String expectedMsg = "带代码的异常消息";
        BusinessException exception = new BusinessException(expectedCode, expectedMsg);
        assertEquals(expectedMsg, exception.getMessage());
        assertEquals(expectedCode, exception.getCode());
    }
}
