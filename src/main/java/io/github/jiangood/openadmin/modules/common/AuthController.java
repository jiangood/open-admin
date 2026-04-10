package io.github.jiangood.openadmin.modules.common;// src/main/java/com/example/controller/AuthController.java

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ICaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.security.SecurityHolder;
import io.github.jiangood.openadmin.lang.PasswordTool;
import io.github.jiangood.openadmin.lang.RsaTool;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.modules.common.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String CAPTCHA_CODE = "captchaCode";


    private final  CodeGenerator codeGenerator;
    private final SecurityHolder securityHolder;
    private final LoginAttemptService loginAttemptService;
    private final SystemProperties systemProperties;


    @PostMapping("login")
    public AjaxResult login(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String captchaCode = loginRequest.getCaptchaCode();
        String sessionCode = (String) session.getAttribute(CAPTCHA_CODE);
        password = this.decodeWebPassword(password);

        // 验证码检查
        checkCaptchaCode(captchaCode, sessionCode);
        // 错误次数检查
        checkAttempts(username);


        // spring security 认证, 参考UsernamePasswordAuthenticationFilter,及其父类AbstractAuthenticationProcessingFilter
        {
            AuthenticationManager authenticationManager = securityHolder.getSharedObject(AuthenticationManager.class);
            SessionAuthenticationStrategy sessionStrategy = securityHolder.getSharedObject(SessionAuthenticationStrategy.class);
            SecurityContextRepository securityContextRepository = securityHolder.getSharedObject(SecurityContextRepository.class);


            try {
                // 1. 认证
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
                Authentication authentication = authenticationManager.authenticate(token);

                // 2. 执行 Session 策略
                sessionStrategy.onAuthentication(authentication, request, response);

                // 3. 认证成功后续
                SecurityContextHolder.getContext().setAuthentication(authentication);
                securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
                loginAttemptService.onSuccess(username); // 登录成功清除记录
            }
            catch (SessionAuthenticationException e) {
                return AjaxResult.err("账号已在其他设备登录，本次登录被拒绝");
            }
            catch (AuthenticationException e) {
                loginAttemptService.onFailed(username);
                log.error("登录失败",e);
                return AjaxResult.err("账号或密码错误");
            }
        }

        return AjaxResult.ok("登录成功");
    }

    private void checkAttempts(String username) {
        int remainingAttempts = loginAttemptService.getRemainingAttempts(username);
        if (remainingAttempts > 0) {
            // 计算延迟时间：基础延迟 1秒，每次失败后翻倍
            int maxAttempts = systemProperties.getLoginLockMaxAttempts();
            int failedAttempts = maxAttempts - remainingAttempts;
            long delayMs = 1000L * (1L << Math.min(failedAttempts, 3)); // 最大延迟 8秒，防止DoS攻击
            ThreadUtil.sleep(delayMs);
        }

        // 检查账户是否被锁定
        boolean locked = loginAttemptService.isAccountLocked(username);
        Assert.state(!locked, "账户已被锁定，请" + systemProperties.getLoginLockMinutes() + "分钟后再试");
    }

    private void checkCaptchaCode(String captchaCode, String sessionCode) {
        if (systemProperties.isCaptcha()) {
            Assert.hasText(captchaCode, "请输入验证码");
            boolean verify = codeGenerator.verify(sessionCode, captchaCode);
            Assert.state(verify, "验证码错误");
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


    @PostMapping("logout")
    public AjaxResult logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        return AjaxResult.ok();
    }


    @GetMapping("captchaImage")
    public void captcha(HttpSession session, HttpServletResponse response) throws IOException {
        log.info("正在生成验证码, sessionId={}", session.getId());
        try {
            ICaptcha captcha = CaptchaUtil.createLineCaptcha(100, 50, codeGenerator, 100);

            String code = captcha.getCode();
            session.setAttribute(CAPTCHA_CODE, code);

            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            captcha.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("生成验证码失败，将验证码参数设置为禁用");
        }
    }


}
