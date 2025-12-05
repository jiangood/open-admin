package io.admin.common.utils;

import cn.hutool.core.text.PasswdStrength;
import cn.hutool.core.util.RandomUtil;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import static cn.hutool.core.util.RandomUtil.BASE_CHAR_NUMBER;

public class PasswordUtils {

    public static String random(){
        return RandomUtil.randomString(BASE_CHAR_NUMBER +"_-!.@$^&*()+=",12);
    }

    /**
     * 生产密码的密文，每次调用都不一样
     * @param plainText
     *
     */
    public static String encode(String plainText) {
        return getPasswordEncoder().encode(plainText);
    }


    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(){
        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            // 特殊处理，密码相同时返回true
            if(rawPassword.toString().equals(encodedPassword)){
                return true;
            }
            return super.matches(rawPassword, encodedPassword);
        }
    };

    public static PasswordEncoder getPasswordEncoder() {
        return PASSWORD_ENCODER;
    }


    /**
     * 校验密码强度
     * @param password
     */
    public static void validateStrength(String password) {
        Assert.state(isStrengthOk(password), "密码强度太低");

    }

    public static boolean isStrengthOk(String password) {
        return PasswdStrength.getLevel(password).ordinal() > PasswdStrength.PASSWD_LEVEL.EASY.ordinal();
    }

}
