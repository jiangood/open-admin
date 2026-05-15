package io.github.jiangood.openadmin.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class RsaTool {

    private static final File KEY_FILE = new File("data/rsa-key");

    private static volatile RSA rsa;

    public static String getPublicKey() {
        return get().getPublicKeyBase64();
    }

    public static String decryptStr(String password, KeyType keyType) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        return get().decryptStr(password, keyType);
    }

    private static RSA get() {
        if (rsa != null) {
            return rsa;
        }
        synchronized (RsaTool.class) {
            if (rsa != null) {
                return rsa;
            }
            rsa = loadOrGenerate();
        }
        return rsa;
    }

    private static RSA loadOrGenerate() {
        if (KEY_FILE.exists()) {
            try {
                List<String> lines = FileUtil.readLines(KEY_FILE, StandardCharsets.UTF_8);
                if (lines.size() >= 2) {
                    String k1 = lines.get(0).trim();
                    String k2 = lines.get(1).trim();
                    if (StrUtil.isAllNotBlank(k1, k2)) {
                        return new RSA(k1, k2);
                    }
                }
            } catch (Exception e) {
                log.warn("读取RSA密钥文件失败，将重新生成", e);
            }
        }

        RSA rsa = new RSA();
        try {
            FileUtil.mkParentDirs(KEY_FILE);
            String content = rsa.getPrivateKeyBase64() + "\n" + rsa.getPublicKeyBase64();
            FileUtil.writeString(content, KEY_FILE, StandardCharsets.UTF_8);
            log.info("RSA密钥已生成并保存到 {}", KEY_FILE.getAbsolutePath());
        } catch (Exception e) {
            log.warn("保存RSA密钥文件失败，仅本次运行有效", e);
        }
        return rsa;
    }
}
