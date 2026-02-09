package io.github.jiangood.openadmin.modules.common;// src/main/java/com/example/controller/AuthController.java

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ICaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String CAPTCHA_CODE = "captchaCode";


    private final  CodeGenerator codeGenerator;



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
