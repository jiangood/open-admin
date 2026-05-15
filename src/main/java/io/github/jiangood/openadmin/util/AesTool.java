package io.github.jiangood.openadmin.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * AES 加密解密工具
 */
@Slf4j
public class AesTool {

    private static final File KEY_FILE = new File("data/aes-key");

    private static volatile String KEY;

    public static String encryptHex(String text) {
        if (text == null) {
            return null;
        }
        return SecureUtil.aes(getKey().getBytes()).encryptHex(text);
    }

    public static String decryptHex(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        try {
            return SecureUtil.aes(getKey().getBytes()).decryptStr(encryptedText);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getKey() {
        if (KEY != null) {
            return KEY;
        }
        synchronized (AesTool.class) {
            if (KEY != null) {
                return KEY;
            }
            KEY = loadOrGenerate();
        }
        return KEY;
    }

    private static String loadOrGenerate() {
        if (KEY_FILE.exists()) {
            try {
                String key = FileUtil.readString(KEY_FILE, StandardCharsets.UTF_8).trim();
                if (!key.isEmpty()) {
                    return key;
                }
            } catch (Exception e) {
                log.warn("读取AES密钥文件失败，将重新生成", e);
            }
        }

        String key = RandomUtil.randomString(16);
        try {
            FileUtil.mkParentDirs(KEY_FILE);
            FileUtil.writeString(key, KEY_FILE, StandardCharsets.UTF_8);
            log.info("AES密钥已生成并保存到 {}", KEY_FILE.getAbsolutePath());
        } catch (Exception e) {
            log.warn("保存AES密钥文件失败，仅本次运行有效", e);
        }
        return key;
    }
}
