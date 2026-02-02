package io.github.jiangood.openadmin.lang;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import org.springframework.util.Assert;

/**
 * AES 加密解密工具
 */
public class AesTool {
    private static String KEY = RandomUtil.randomString(16);
    private static boolean KEY_CAN_CHANGE = true;


    public static void initKey(String newKey) {
        Assert.notNull(newKey, "Aes的Key不能为空");
        Assert.state(KEY_CAN_CHANGE, "Aes的Key只能初始化一次");
        KEY = newKey;
        KEY_CAN_CHANGE = false;
    }


    /**
     * 加密
     *
     * @param text 明文
     * @return 密文，并使用hex编码
     */
    public static String encryptHex(String text) {
        if (text == null) {
            return null;
        }
        return SecureUtil.aes(KEY.getBytes()).encryptHex(text);
    }

    /**
     * 解密
     *
     * @param encryptedText 密文
     * @return 明文
     */
    public static String decryptHex(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        try {
            return SecureUtil.aes(KEY.getBytes()).decryptStr(encryptedText);
        } catch (Exception e) {
            return null;
        }
    }
}
