package io.admin.framework.config.security;

import io.admin.common.dto.AjaxResult;
import io.admin.common.utils.ArrayUtils;
import io.admin.common.utils.PasswordUtils;
import io.admin.common.utils.ResponseUtils;
import io.admin.framework.config.SysProp;
import io.admin.framework.config.init.SystemHook;
import io.admin.framework.config.init.SystemHookService;
import io.admin.framework.config.security.refresh.PermissionRefreshFilter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.List;

@Slf4j
@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final SysProp sysProp;

    private SystemHookService systemHookService;

    // 配置 HTTP 安全
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginFilter loginFilter, PermissionRefreshFilter permissionRefreshFilter) throws Exception {
        String[] loginExclude = ArrayUtils.toStrArr(sysProp.getXssExcludePathList());

        systemHookService.beforeConfigSecurity(http);
        // 前后端分离项目，关闭csrf
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authz -> {
            if (loginExclude.length > 0) {
                authz.requestMatchers(loginExclude).permitAll();
            }
            authz.requestMatchers("/admin/auth/**", "/admin/public/**").permitAll();
            authz.requestMatchers("/admin/**", "/ureport/**").authenticated();
            authz.anyRequest().permitAll();
        });

        http.formLogin(cfg -> {
            cfg.loginProcessingUrl("/admin/auth/login").defaultSuccessUrl("/admin/auth/success").successHandler((request, response, authentication) -> {
                AjaxResult rs = AjaxResult.ok("登录成功");
                ResponseUtils.response(response, rs);
            }).failureHandler((request, response, exception) -> {
                AjaxResult rs = AjaxResult.err("登录失败" + exception.getMessage());
                ResponseUtils.response(response, rs);
            });
        });

        http.sessionManagement(cfg -> {
            int maximumSessions = sysProp.getMaximumSessions();
            log.info("设置最大并发会话数为 {}", maximumSessions);

            cfg.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
            cfg.sessionConcurrency(conCfg -> {
                conCfg.maximumSessions(maximumSessions)
                        //达到限制时，新登录失败
                        //.maxSessionsPreventsLogin(true)
                        .sessionRegistry(sessionRegistry());
            });
        });
        http.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(permissionRefreshFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(cfg -> {
            cfg.accessDeniedHandler((request, response, e) -> {
                AjaxResult err = AjaxResult.err(e.getMessage());
                ResponseUtils.response(response, err);
            }).authenticationEntryPoint((request, response, e) -> {
                AjaxResult err = AjaxResult.err(401, "认证信息已失效或未登录，请重新登录。" + e.getMessage());
                response.setStatus(401);
                ResponseUtils.response(response, err);
            });

        });


        return http.build();

    }


    // 密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordUtils.getPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    @Bean
    @ConditionalOnMissingBean // 这样用户可以自定义redisSession等来覆盖
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * **重要**：注册一个 SessionRegistry Bean
     * SessionRegistry 用于记录和管理所有的 Session 信息，是并发控制的基础。
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        // 使用标准的 SessionRegistryImpl 即可
        return new SessionRegistryImpl();
    }


}
