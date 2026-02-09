package io.github.jiangood.openadmin.lang;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.extra.spring.SpringUtil;
import io.github.jiangood.openadmin.framework.config.SysProperties;

public class RsaTool {

    private static RSA rsa;

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
        SysProperties p = SpringUtil.getBean(SysProperties.class);
        String k1 = p.getRsaPrivateKey();
        String k2 = p.getRsaPublicKey();
        if (StrUtil.isAllBlank(k1, k2)) {
            rsa = new RSA();
        } else {
            rsa = new RSA(k1, k2);
        }
        return rsa;
    }

    public static void main(String[] args) {
        System.out.println(rsa.getPrivateKeyBase64());
        System.out.println(rsa.getPublicKeyBase64());

    }
}
