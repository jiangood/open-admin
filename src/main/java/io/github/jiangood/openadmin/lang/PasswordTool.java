package io.github.jiangood.openadmin.lang;

import cn.hutool.core.text.PasswdStrength;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordTool {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder() ;

    /**
     * 生成随机密码
     * <p>
     * 生成的密码包含数字、小写字母、大写字母和特殊字符，长度为12位
     * </p>
     * 
     * @return 随机生成的密码字符串
     */
    public static String random() {
        // 定义不同类型的字符集
        String digits = "0123456789";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String specialChars = "_-!.@$^&*()+=~";
        
        // 确保至少包含每种类型的一个字符
        StringBuilder password = new StringBuilder();
        password.append(digits.charAt(cn.hutool.core.util.RandomUtil.randomInt(digits.length())));
        password.append(lowerCase.charAt(cn.hutool.core.util.RandomUtil.randomInt(lowerCase.length())));
        password.append(upperCase.charAt(cn.hutool.core.util.RandomUtil.randomInt(upperCase.length())));
        password.append(specialChars.charAt(cn.hutool.core.util.RandomUtil.randomInt(specialChars.length())));
        
        // 填充剩余字符
        String allChars = digits + lowerCase + upperCase + specialChars;
        String pad = RandomUtil.randomString(allChars, 8);
        password.append(pad);

        String shuffle = StringTool.shuffle(password.toString());

        return shuffle;
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
