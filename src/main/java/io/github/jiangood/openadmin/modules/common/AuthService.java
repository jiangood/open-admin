package io.github.jiangood.openadmin.modules.common;// 文件名: CustomLoginFilter.java

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import io.github.jiangood.openadmin.lang.PasswordTool;
import io.github.jiangood.openadmin.lang.RsaTool;
import io.github.jiangood.openadmin.framework.config.SysProperties;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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
    SysProperties prop;

    @Resource
    CodeGenerator codeGenerator;


    /**
     * @param request
     * @return 解密后的密码
     */
    public void validate(HttpServletRequest request) {
        // 0. 随眠 1秒，对用户无感知，但等防止爆破攻击
        ThreadUtil.sleep(1000);

        String username = request.getParameter("username");
        String captchaCode = request.getParameter("captchaCode");


        boolean locked = loginAttemptService.isAccountLocked(username);
        Assert.state(!locked, "账户已被锁定，请" + prop.getLoginLockMinutes() + "分钟后再试");


        // 验证码校验
        if (prop.isCaptcha()) {
            HttpSession session = request.getSession(false);
            Assert.notNull(session, "页面已失效，请刷新页面");

            Assert.hasText(captchaCode, "请输入验证码");
            String sessionCode = (String) session.getAttribute(CAPTCHA_CODE);
            Assert.state(codeGenerator.verify(sessionCode, captchaCode), "验证码错误");
        }


    }

    public String decodeWebPassword(String password) {
        try {
            password = RsaTool.decryptStr(password, KeyType.PrivateKey);
        } catch (Exception e) {
            log.error("输入密码解密失败: {}", e.getMessage());
            throw new IllegalStateException("密码未加密");
        }

        boolean strengthOk = PasswordTool.isStrengthOk(password);
        Assert.state(strengthOk, "密码强度不够，请联系管理员重置");
        return password;
    }


    public void onSuccess(String username) {
        loginAttemptService.loginSucceeded(username); // 登录成功清除记录
    }

    public void onFail(String username) {
        loginAttemptService.loginFailed(username);
    }
}
