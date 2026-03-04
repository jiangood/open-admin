package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PasswordTool工具类的单元测试
 */
class PasswordToolTest {

    @Test
    void testRandom() {
        for (int i = 0; i < 1000; i++) {
            // 测试生成随机密码
            String password1 = PasswordTool.random();
            assertNotNull(password1);
            assertFalse(password1.isEmpty());

            // 测试密码长度（应该是12位）
            assertEquals(12, password1.length());

            // 测试多次生成的密码不同
            String password2 = PasswordTool.random();
            String password3 = PasswordTool.random();

            assertNotNull(password2);
            assertNotNull(password3);
            assertNotEquals(password1, password2, "多次生成的随机密码应该不同");
            assertNotEquals(password2, password3, "多次生成的随机密码应该不同");
            assertNotEquals(password1, password3, "多次生成的随机密码应该不同");

            // 测试密码包含的字符类型（应该包含字母、数字和特殊字符）
            boolean hasLetter = false;
            boolean hasNumber = false;
            boolean hasSpecialChar = false;

            for (char c : password1.toCharArray()) {
                if (Character.isLetter(c)) hasLetter = true;
                if (Character.isDigit(c)) hasNumber = true;
                if (!Character.isLetterOrDigit(c)) hasSpecialChar = true;
            }

            assertTrue(hasLetter, "随机密码应该包含字母");
            assertTrue(hasNumber, "随机密码应该包含数字");
            assertTrue(hasSpecialChar, "随机密码应该包含特殊字符");
        }
    }

    @Test
    void testEncode() {
        // 测试密码加密
        String plainPassword = "testPassword123";
        String encodedPassword1 = PasswordTool.encode(plainPassword);
        
        assertNotNull(encodedPassword1);
        assertFalse(encodedPassword1.isEmpty());
        assertNotEquals(plainPassword, encodedPassword1, "加密后的密码应该与原密码不同");
        
        // 测试同一密码多次加密结果不同（BCrypt特性）
        String encodedPassword2 = PasswordTool.encode(plainPassword);
        String encodedPassword3 = PasswordTool.encode(plainPassword);
        
        assertNotNull(encodedPassword2);
        assertNotNull(encodedPassword3);
        assertNotEquals(encodedPassword1, encodedPassword2, "同一密码多次加密应该产生不同的结果");
        assertNotEquals(encodedPassword2, encodedPassword3, "同一密码多次加密应该产生不同的结果");
        
        // 测试不同密码加密结果不同
        String differentPassword = "differentPassword456";
        String encodedDifferent = PasswordTool.encode(differentPassword);
        
        assertNotEquals(encodedPassword1, encodedDifferent, "不同密码的加密结果应该不同");
        
        // 测试空密码
        String emptyPassword = "";
        String encodedEmpty = PasswordTool.encode(emptyPassword);
        assertNotNull(encodedEmpty);
        
        // 测试密码编码器
        PasswordEncoder encoder = PasswordTool.getEncoder();
        assertNotNull(encoder);
        
        // 测试密码匹配
        assertTrue(encoder.matches(plainPassword, encodedPassword1), "密码应该匹配");
        assertFalse(encoder.matches("wrongPassword", encodedPassword1), "错误密码不应该匹配");
        
        assertFalse(encoder.matches(plainPassword, plainPassword), "密码相同时应该返回false");
    }

    @Test
    void testGetPasswordEncoder() {
        // 测试获取密码编码器
        PasswordEncoder encoder = PasswordTool.getEncoder();
        
        assertNotNull(encoder);
        
        // 测试编码器的基本功能
        String password = "testPassword";
        String encoded = encoder.encode(password);
        
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
        assertTrue(encoder.matches(password, encoded), "编码器应该正确匹配密码");
        assertFalse(encoder.matches("wrongPassword", encoded), "编码器应该拒绝错误密码");
    }

    @Test
    void testIsStrengthOk() {
        // 测试弱密码
        assertFalse(PasswordTool.isStrengthOk("123"), "纯数字密码应该被认为是弱密码");
        assertFalse(PasswordTool.isStrengthOk("abc"), "纯字母密码应该被认为是弱密码");
        assertFalse(PasswordTool.isStrengthOk("abc123"), "简单字母数字组合应该被认为是弱密码");
        assertFalse(PasswordTool.isStrengthOk("123456"), "连续数字应该被认为是弱密码");
        assertFalse(PasswordTool.isStrengthOk("password"), "常见单词应该被认为是弱密码");
        
        // 测试中等强度密码
        assertTrue(PasswordTool.isStrengthOk("abc123!"), "包含特殊字符的密码应该被认为是中等强度");
        assertTrue(PasswordTool.isStrengthOk("Abc123!"), "包含大小写字母、数字和特殊字符的密码应该被认为是中等强度");
        
        // 测试强密码
        assertTrue(PasswordTool.isStrengthOk("Abcdef123!@#"), "复杂密码应该被认为是强密码");
        assertTrue(PasswordTool.isStrengthOk("MyP@ssw0rd!23"), "复杂密码应该被认为是强密码");
        
        // 测试空密码（会抛出异常）
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordTool.isStrengthOk("");
        }, "空密码应该抛出IllegalArgumentException");
        
        // 测试null密码（会抛出异常）
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordTool.isStrengthOk(null);
        }, "null密码应该抛出IllegalArgumentException");
        
        // 测试随机生成的密码强度
        String randomPassword = PasswordTool.random();
        assertTrue(PasswordTool.isStrengthOk(randomPassword), "随机生成的密码应该满足强度要求");
    }

    @Test
    void testValidateStrength() {
        // 测试验证强密码（不应该抛出异常）
        assertDoesNotThrow(() -> {
            PasswordTool.validateStrength("Abc123!@#");
        }, "强密码验证不应该抛出异常");
        
        assertDoesNotThrow(() -> {
            PasswordTool.validateStrength("MyP@ssw0rd!23");
        }, "强密码验证不应该抛出异常");
        
        // 测试验证弱密码（应该抛出异常）
        assertThrows(IllegalStateException.class, () -> {
            PasswordTool.validateStrength("123");
        }, "弱密码验证应该抛出IllegalStateException");
        
        assertThrows(IllegalStateException.class, () -> {
            PasswordTool.validateStrength("abc");
        }, "弱密码验证应该抛出IllegalStateException");
        
        assertThrows(IllegalStateException.class, () -> {
            PasswordTool.validateStrength("password");
        }, "弱密码验证应该抛出IllegalStateException");
        
        // 测试验证空密码（应该抛出异常）
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordTool.validateStrength("");
        }, "空密码验证应该抛出异常");
        
        // 测试验证null密码（应该抛出异常）
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordTool.validateStrength(null);
        }, "null密码验证应该抛出异常");
    }
}
