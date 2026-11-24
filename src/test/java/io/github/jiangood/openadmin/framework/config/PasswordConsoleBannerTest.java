package io.github.jiangood.openadmin.framework.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordConsoleBannerTest {

    @Test
    void passwordBlockShouldContainMarkerAccountAndPassword() {
        String block = PasswordConsoleBanner.renderPasswordBlock(
                "[ADMIN-PASSWORD-RESET]", "SUCCESS", "admin", "GEN-A1b!cD2eF3gH");

        assertTrue(block.contains("[ADMIN-PASSWORD-RESET]"));
        assertTrue(block.contains("SUCCESS"));
        assertTrue(block.contains("admin"));
        assertTrue(block.contains("GEN-A1b!cD2eF3gH"));
        assertTrue(block.contains("NEW PASSWORD"));
    }

    @Test
    void notChangedBlockShouldNotContainPasswordPlaceholder() {
        String block = PasswordConsoleBanner.renderNotChangedBlock("admin", true);

        assertTrue(block.contains("[ADMIN-PASSWORD-CHECK]"));
        assertTrue(block.contains("ADMIN PASSWORD HAS NOT BEEN CHANGED"));
        assertTrue(block.contains("--reset-admin-password"));
        assertFalse(block.contains("NEW PASSWORD"));
    }

    @Test
    void allBannersShouldBeAsciiOnly() {
        assertAscii(PasswordConsoleBanner.renderPasswordBlock(
                "[INITIAL-ADMIN-PASSWORD]", "ADMIN ACCOUNT CREATED", "admin", "GEN-A1b!cD2eF3gH"));
        assertAscii(PasswordConsoleBanner.renderNotChangedBlock("admin", false));
        assertAscii(PasswordConsoleBanner.renderFailureBlock("[ADMIN-PASSWORD-RESET]", "ADMIN ACCOUNT NOT FOUND"));
    }

    @Test
    void nonAsciiFailureReasonShouldFallBackToEnglish() {
        String fallback = PasswordConsoleBanner.asciiOrFallback("管理员密码重置失败");
        assertAscii(fallback);
        assertTrue(fallback.contains("Unexpected error"));

        assertTrue(PasswordConsoleBanner.asciiOrFallback("ADMIN ACCOUNT NOT FOUND").contains("ADMIN ACCOUNT NOT FOUND"));
    }

    private void assertAscii(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            assertTrue(c >= 0x20 && c <= 0x7E || c == '\r' || c == '\n',
                    "横幅必须全 ASCII，避免控制台乱码，发现非 ASCII 字符: " + c);
        }
    }
}
