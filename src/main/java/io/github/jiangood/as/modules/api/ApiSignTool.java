package io.github.jiangood.as.modules.api;

import cn.hutool.crypto.SecureUtil;

public class ApiSignTool {

    public static String sign(String appId, String appSecret, long timestamp) {
        String signStr = appId + appSecret + timestamp;
        return SecureUtil.md5(signStr);
    }

}
