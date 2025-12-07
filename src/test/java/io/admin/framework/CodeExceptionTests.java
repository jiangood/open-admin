package io.admin.framework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CodeExceptionTests {

    @Test
    void testCodeExceptionDefaultConstructor() {
        CodeException exception = new CodeException();
        
        assertNull(exception.getMessage());
        assertEquals(0, exception.getCode());
    }

    @Test
    void testCodeExceptionWithMessage() {
        CodeException exception = new CodeException("Test message");
        
        assertEquals("Test message", exception.getMessage());
        assertEquals(0, exception.getCode());
    }

    @Test
    void testCodeExceptionWithPrefixAndThrowable() {
        IllegalArgumentException cause = new IllegalArgumentException("Cause message");
        CodeException exception = new CodeException("Prefix", cause);
        
        assertEquals("Prefix: Cause message", exception.getMessage());
        assertEquals(0, exception.getCode()); // code默认为0
    }

    @Test
    void testCodeExceptionWithCodeAndMessage() {
        CodeException exception = new CodeException(404, "Not Found");
        
        assertEquals("Not Found", exception.getMessage());
        assertEquals(404, exception.getCode());
    }

    @Test
    void testExceptionInheritance() {
        CodeException exception = new CodeException("Test");
        
        // 验证继承自IllegalStateException
        assertTrue(exception instanceof IllegalStateException);
    }
}