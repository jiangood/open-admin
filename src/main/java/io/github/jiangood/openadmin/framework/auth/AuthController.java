package io.github.jiangood.openadmin.framework.auth;

import cn.hutool.core.thread.ThreadUtil;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.security.SecurityHolder;
import io.github.jiangood.openadmin.framework.ratelimit.RateLimit;
import io.github.jiangood.openadmin.util.PasswordTool;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.auth.dto.LoginReq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SecurityHolder securityHolder;
    private final LoginAttemptService loginAttemptService;
    private final SystemProperties systemProperties;

    @PostMapping("login")
    @RateLimit(count = 10, duration = 60)
    public AjaxResult login(@RequestBody @Valid LoginReq loginRequest, HttpServletRequest request, HttpServletResponse response) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        password = this.decodeWebPassword(password);

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
                return AjaxResult.err("账号或密码错误");
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

    private String decodeWebPassword(String password) {
        try {
            byte[] data = Base64.getDecoder().decode(password);
            byte[] result = new byte[data.length];
            for (int k = 0; k < data.length; k++) {
                result[k] = (byte)(data[k] - k - 2);
            }
            password = new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("密码解析失败");
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

}
