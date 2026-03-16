package io.github.jiangood.openadmin.framework.config.security.login;// 文件名: CustomLoginFilter.java

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.lang.PasswordTool;
import io.github.jiangood.openadmin.lang.RsaTool;
import io.github.jiangood.openadmin.modules.common.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 登录逻辑验证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthTokenProvider implements AuthenticationProvider {


    public static final String MSG_REFRESH = "页面已失效，请刷新浏览器后重试";
    public static final String MSG_USERNAME_OR_PASSWORD = "账号或密码错误";

    private final LoginAttemptService loginAttemptService;

    private final SystemProperties systemProperties;

    private final CodeGenerator codeGenerator;

    private final UserDetailsService userDetailsService;


    @Override
    public boolean supports(Class<?> authentication) {
        return AuthToken.class.isAssignableFrom(authentication);
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof AuthToken authToken)) {
            return null;
        }

        String username = (String) authToken.getPrincipal();
        String captchaCode = authToken.getCaptchaCode();
        String password = (String) authToken.getCredentials();
        String sessionCode = authToken.getSessionCode();

        // 验证码校验
        if (systemProperties.isCaptcha()) {
            if (StrUtil.isBlank(captchaCode)) throw new AccessDeniedException("请输入验证码");

            boolean verify = codeGenerator.verify(sessionCode, captchaCode);

            if (!verify) throw new AccessDeniedException("验证码错误");
        }

        // 加载用户详情（不验证密码）
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 检查用户状态
        checkUserStatus(userDetails);


        // 解密
        password = this.decodeWebPassword(password);
        if (password == null) {
            throw new AccessDeniedException(MSG_REFRESH);
        }

        int remainingAttempts = loginAttemptService.getRemainingAttempts(username);
        if (remainingAttempts > 0) {
            // 计算延迟时间：基础延迟 1秒，每次失败后翻倍
            int maxAttempts = systemProperties.getLoginLockMaxAttempts();
            int failedAttempts = maxAttempts - remainingAttempts;
            long delayMs = 1000L * (1L << Math.min(failedAttempts, 3)); // 最大延迟 8秒，防止DoS攻击
            ThreadUtil.sleep(delayMs);
        }

        // 校验密码
        boolean matches = PasswordTool.getEncoder().matches(password, userDetails.getPassword());
        if (matches) {
            loginAttemptService.loginSucceeded(username); // 登录成功清除记录
        } else {
            loginAttemptService.loginFailed(username);
            throw new AccessDeniedException(MSG_USERNAME_OR_PASSWORD);
        }


        // 检查账户是否被锁定
        boolean locked = loginAttemptService.isAccountLocked(username);
        Assert.state(!locked, "账户已被锁定，请" + systemProperties.getLoginLockMinutes() + "分钟后再试");


        // 创建已认证的令牌
        AuthToken token = new AuthToken(userDetails, userDetails.getAuthorities());
        token.setDetails(userDetails);
        return token;
    }


    private void checkUserStatus(UserDetails userDetails) {
        if (!userDetails.isEnabled()) {
            throw new DisabledException("用户已被禁用");
        }
        if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("用户已被锁定");
        }
        if (!userDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("用户账号已过期");
        }
    }


    private String decodeWebPassword(String password) {
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
}
