package io.admin.framework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CodeAssertTests {

    @Test
    void testStateWithTrueCondition() {
        // 当条件为true时，不应该抛出异常
        assertDoesNotThrow(() -> {
            CodeAssert.state(true, 500, "This should not be thrown");
        });
    }

    @Test
    void testStateWithFalseCondition() {
        // 当条件为false时，应该抛出CodeException
        CodeException exception = assertThrows(CodeException.class, () -> {
            CodeAssert.state(false, 400, "Error message");
        });
        
        assertEquals(400, exception.getCode());
        assertEquals("Error message", exception.getMessage());
    }
}