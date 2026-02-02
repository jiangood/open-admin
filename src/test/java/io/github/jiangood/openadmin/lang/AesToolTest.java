package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AesTool工具类的单元测试
 */
class AesToolTest {

    @Test
    void testEncryptHexAndDecryptHex() {
        String original = "test message";
        
        // 测试加密
        String encrypted = AesTool.encryptHex(original);
        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);
        
        // 测试解密
        String decrypted = AesTool.decryptHex(encrypted);
        assertNotNull(decrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void testEncryptHexWithNull() {
        // 测试空原文
        String encrypted = AesTool.encryptHex(null);
        assertNull(encrypted);
    }

    @Test
    void testDecryptHexWithNull() {
        // 测试空密文
        String decrypted = AesTool.decryptHex(null);
        assertNull(decrypted);
    }

    @Test
    void testDecryptHexWithInvalidCipher() {
        // 测试无效的密文
        String decrypted = AesTool.decryptHex("invalid cipher");
        assertNull(decrypted);
    }
}
