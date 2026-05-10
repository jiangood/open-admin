package io.github.jiangood.openadmin.framework.auth;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ICaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.security.SecurityHolder;
import io.github.jiangood.openadmin.framework.ratelimit.RateLimit;
import io.github.jiangood.openadmin.util.PasswordTool;
import io.github.jiangood.openadmin.util.RsaTool;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.auth.dto.LoginReq;
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

    private final CodeGenerator codeGenerator;
    private final SecurityHolder securityHolder;
    private final LoginAttemptService loginAttemptService;
    private final SystemProperties systemProperties;

    @PostMapping("login")
    @RateLimit(count = 10, duration = 60)
    public AjaxResult login(@RequestBody @Valid LoginReq loginRequest, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String captchaCode = loginRequest.getCaptchaCode();
        String sessionCode = (String) session.getAttribute(CAPTCHA_CODE);
        password = this.decodeWebPassword(password);

        checkCaptchaCode(captchaCode, sessionCode);
        checkAttempts(username);

        {
            AuthenticationManager authenticationManager = securityHolder.getSharedObject(AuthenticationManager.class);
            SessionAuthenticationStrategy sessionStrategy = securityHolder.getSharedObject(SessionAuthenticationStrategy.class);
            SecurityContextRepository securityContextRepository = securityHolder.getSharedObject(SecurityContextRepository.class);

            try {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
                Authentication authentication = authenticationManager.authenticate(token);

                sessionStrategy.onAuthentication(authentication, request, response);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
                loginAttemptService.onSuccess(username);
            }
            catch (SessionAuthenticationException e) {
                return AjaxResult.err("账号已在其他设备登录，本次登录被拒绝");
            }
            catch (AuthenticationException e) {
                loginAttemptService.onFailed(username);
                log.error("登录失败", e);
                return AjaxResult.err("账号或密码错误");
            }
        }

        return AjaxResult.ok("登录成功");
    }

    private void checkAttempts(String username) {
        int remainingAttempts = loginAttemptService.getRemainingAttempts(username);
        if (remainingAttempts > 0) {
            int maxAttempts = systemProperties.getLoginLockMaxAttempts();
            int failedAttempts = maxAttempts - remainingAttempts;
            long delayMs = 1000L * (1L << Math.min(failedAttempts, 3));
            ThreadUtil.sleep(delayMs);
        }

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

    @GetMapping("captcha-image")
    @RateLimit(count = 30, duration = 60)
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
