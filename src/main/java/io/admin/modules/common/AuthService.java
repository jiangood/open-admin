package io.admin.modules.common;// 文件名: CustomLoginFilter.java

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import io.admin.common.utils.JsonUtils;
import io.admin.common.utils.PasswordUtils;
import io.admin.modules.common.dto.LoginRequest;
import io.admin.modules.system.ConfigConsts;
import io.admin.framework.config.SysProp;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * 自定义的登录逻辑
 */
@Slf4j
@Component
public class AuthService {



    public static final String CAPTCHA_CODE = "captchaCode";

    @Resource
    LoginAttemptService loginAttemptService;

    @Resource
    SysProp prop;

    @Resource
    CodeGenerator codeGenerator;



    public void preHandler(HttpServletRequest request) throws IOException {
        // 0. 随眠 1秒，对用户无感知，但等防止爆破攻击
        ThreadUtil.sleep(1000);

        String body = IoUtil.readUtf8(request.getInputStream());
        LoginRequest loginRequest = JsonUtils.jsonToBean(body, LoginRequest.class);
        String username = loginRequest.getAccount();
        String password = loginRequest.getPassword();

        boolean locked = loginAttemptService.isAccountLocked(username);
        Assert.state(!locked, "账户已被锁定，请" + prop.getLoginLockMinutes() + "分钟后再试");

        boolean strengthOk = PasswordUtils.isStrengthOk(password);
        Assert.state(strengthOk, "密码强度不够，请联系管理员重置");

        // 验证码校验
        if (prop.isCaptcha()) {
            HttpSession session = request.getSession(false);
            Assert.hasText(loginRequest.getCode(), "请输入验证码");
            String sessionCode = (String) session.getAttribute(CAPTCHA_CODE);
            Assert.state(codeGenerator.verify(sessionCode, loginRequest.getCode()), "验证码错误");
            session.removeAttribute(CAPTCHA_CODE);
        }


        // 解密前端密码
        String rsaPrivateKey = ConfigConsts.get(ConfigConsts.RSA_PRIVATE_KEY);
        String rsaPublicKey = ConfigConsts.get(ConfigConsts.RSA_PUBLIC_KEY);
        RSA rsa = SecureUtil.rsa(rsaPrivateKey, rsaPublicKey);
        password = rsa.decryptStr(password, KeyType.PrivateKey);

        request.setAttribute("username", username);
        request.setAttribute("password",password);
    }



    public void onSuccess(String username)  {
        loginAttemptService.loginSucceeded(username); // 登录成功清除记录
    }

    public void onFail(String username) {
        loginAttemptService.loginFailed(username);
    }
}
