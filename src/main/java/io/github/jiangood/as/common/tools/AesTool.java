package io.github.jiangood.as.common.tools;

import cn.hutool.crypto.SecureUtil;

/**
 * AES 加密解密工具
 */
public class AesTool {
    private static final String key = "61GmaSlKCd4@mxvc";

    /**
     * 加密
     *
     * @param text 明文
     * @return 密文，并使用hex编码
     */
    public static String encryptHex(String text) {
        return SecureUtil.aes(key.getBytes()).encryptHex(text);
    }

    /**
     * 解密
     *
     * @param encryptedText 密文
     * @return 明文
     */
    public static String decryptHex(String encryptedText) {
        return SecureUtil.aes(key.getBytes()).decryptStr(encryptedText);
    }
}
