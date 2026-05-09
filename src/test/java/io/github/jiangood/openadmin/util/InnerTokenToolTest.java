package io.github.jiangood.openadmin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InnerTokenToolTest {

    @Test
    void testCreateToken() {
        // 测试创建令牌
        String account = "test@example.com";
        String token = InnerTokenTool.createToken(account);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testValidateToken() {
        // 测试验证令牌
        String account = "test@example.com";
        String token = InnerTokenTool.createToken(account);
        String validatedAccount = InnerTokenTool.validateToken(token);
        assertEquals(account, validatedAccount);
    }

    @Test
    void testCreateTokenWithEmptyAccount() {
        // 测试使用空账号创建令牌
        String account = "";
        String token = InnerTokenTool.createToken(account);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // 验证令牌
        String validatedAccount = InnerTokenTool.validateToken(token);
        assertEquals(account, validatedAccount);
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        // 测试验证无效令牌
        String invalidToken = "invalid-token";
        String validatedAccount = InnerTokenTool.validateToken(invalidToken);
        // 这里应该返回 null，因为 AesTool.decryptHex 会处理无效输入
        assertNull(validatedAccount);
    }

    @Test
    void testValidateTokenWithNullToken() {
        // 测试验证 null 令牌
        String validatedAccount = InnerTokenTool.validateToken(null);
        // 这里应该返回 null，因为 AesTool.decryptHex 会处理 null 输入
        assertNull(validatedAccount);
    }

}
