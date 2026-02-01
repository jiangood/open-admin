package io.github.jiangood.openadmin.lang;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.RSA;

public class SysRsaTool {
    private static final RSA rsa = SecureUtil.rsa();

    public static RSA getRsa() {
        return rsa;
    }

    public static String getPublicKey() {
        return rsa.getPublicKeyBase64();
    }

    public static String getPrivateKey() {
        return rsa.getPrivateKeyBase64();
    }


}
