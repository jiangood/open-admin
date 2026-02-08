package io.github.jiangood.openadmin.lang;

import cn.hutool.core.text.PasswdStrength;
import cn.hutool.core.util.RandomUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import static cn.hutool.core.util.RandomUtil.BASE_CHAR_NUMBER;

public class PasswordTool {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder() ;

    public static String random() {
        return RandomUtil.randomString(BASE_CHAR_NUMBER + "_-!.@$^&*()+=", 12);
    }

    /**
     * 生产密码的密文，每次调用都不一样
     *
     * @param plainText
     */
    public static String encode(String plainText) {
        return getEncoder().encode(plainText);
    }

    public static PasswordEncoder getEncoder() {
        return PASSWORD_ENCODER;
    }


    /**
     * 校验密码强度
     *
     * @param password
     */
    public static void validateStrength(String password) {
        Assert.state(isStrengthOk(password), "密码强度太低");

    }

    public static boolean isStrengthOk(String password) {
        return PasswdStrength.getLevel(password).ordinal() > PasswdStrength.PASSWD_LEVEL.EASY.ordinal();
    }

}
