package io.admin.modules.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ApiErrorCodeTests {

    @Test
    void testApiErrorCodeValues() {
        // 测试枚举值的完整性
        ApiErrorCode[] values = ApiErrorCode.values();
        
        assertNotNull(values);
        assertTrue(values.length > 0);
        
        // 测试特定枚举值
        ApiErrorCode accNotFound = ApiErrorCode.ACC_NOT_FOUND;
        assertEquals(1404, accNotFound.getCode());
        assertEquals("账号不存在", accNotFound.getMessage());
        
        ApiErrorCode resNotFound = ApiErrorCode.RES_NOT_FOUND;
        assertEquals(2404, resNotFound.getCode());
        assertEquals("接口不存在", resNotFound.getMessage());
        
        ApiErrorCode permNotFound = ApiErrorCode.PERM_NOT_FOUND;
        assertEquals(3404, permNotFound.getCode());
        assertEquals("未授权", permNotFound.getMessage());
        
        ApiErrorCode globalError = ApiErrorCode.GLOBAL_ERROR;
        assertEquals(5000, globalError.getCode());
        assertEquals("服务器错误", globalError.getMessage());
    }

    @Test
    void testAllErrorCodeProperties() {
        for (ApiErrorCode errorCode : ApiErrorCode.values()) {
            assertTrue(errorCode.getCode() > 0, "Error code should be positive: " + errorCode.name());
            assertNotNull(errorCode.getMessage(), "Error message should not be null: " + errorCode.name());
            assertFalse(errorCode.getMessage().isEmpty(), "Error message should not be empty: " + errorCode.name());
        }
    }
}