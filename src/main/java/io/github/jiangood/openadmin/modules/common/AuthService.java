package io.github.jiangood.openadmin.modules.common;// 文件名: CustomLoginFilter.java

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import io.github.jiangood.openadmin.lang.PasswordTool;
import io.github.jiangood.openadmin.lang.RsaTool;
import io.github.jiangood.openadmin.framework.config.SysProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 自定义的登录逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthService {


    public static final String CAPTCHA_CODE = "captchaCode";

    private final LoginAttemptService loginAttemptService;

    private final SysProperties sysProperties;

    private final CodeGenerator codeGenerator;


    /**
     * @return 解密后的密码
     */
    public void validate(HttpServletRequest request) {
        String username = request.getParameter("username");
        String captchaCode = request.getParameter("captchaCode");

        // 检查账户是否被锁定
        boolean locked = loginAttemptService.isAccountLocked(username);
        Assert.state(!locked, "账户已被锁定，请" + sysProperties.getLoginLockMinutes() + "分钟后再试");

        // 实现指数退避策略的登录延迟，防止暴力破解
        int remainingAttempts = loginAttemptService.getRemainingAttempts(username);
        if (remainingAttempts > 0) {
            // 计算延迟时间：基础延迟 1秒，每次失败后翻倍
            int maxAttempts = sysProperties.getLoginLockMaxAttempts();
            int failedAttempts = maxAttempts - remainingAttempts;
            long delayMs = 1000L * (1L << Math.min(failedAttempts, 3)); // 最大延迟 8秒，防止DoS攻击
            ThreadUtil.sleep(delayMs);
        }

        // 验证码校验
        if (sysProperties.isCaptcha()) {
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
            throw new IllegalStateException("页面已过期，请刷新后重试");
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
