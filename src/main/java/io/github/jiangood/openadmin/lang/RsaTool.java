package io.github.jiangood.openadmin.lang;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.Getter;
import lombok.experimental.Delegate;

public class RsaTool {

    private static final RSA rsa = SecureUtil.rsa();

    public static String getPublicKey() {
        return rsa.getPublicKeyBase64();
    }


    public static String decryptStr(String password, KeyType keyType) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        return rsa.decryptStr(password, keyType);
    }
}
