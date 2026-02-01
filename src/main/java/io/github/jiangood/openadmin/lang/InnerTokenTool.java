package io.github.jiangood.openadmin.lang;


public class InnerTokenTool {


    // 由门户中心调用
    public static String createToken(String account) {
        return AesTool.encryptHex(account);
    }

    // 由子系统验证， 后期调整为可配置
    public static String validateToken(String token) {
        return AesTool.decryptHex(token);
    }


}
