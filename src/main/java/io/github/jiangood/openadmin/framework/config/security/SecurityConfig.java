package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.init.OpenLifecycleManager;
import io.github.jiangood.openadmin.framework.config.security.login.LoginConfigurer;
import io.github.jiangood.openadmin.framework.config.security.refresh.PermissionRefreshFilter;
import io.github.jiangood.openadmin.lang.ArrayTool;
import io.github.jiangood.openadmin.lang.PasswordTool;
import io.github.jiangood.openadmin.lang.ResponseTool;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity  // 必须启用这个注解
public class SecurityConfig {

    private final SystemProperties systemProperties;

    private final OpenLifecycleManager lifecycleHookManager;

    private final PermissionRefreshFilter permissionRefreshFilter;



    // 配置 HTTP 安全
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
        String[] loginExclude = ArrayTool.toStrArr(systemProperties.getXssExcludePathList());

        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
        lifecycleHookManager.onConfigSecurity(http,authenticationManager);

        http.authorizeHttpRequests(authz -> {
            lifecycleHookManager.onConfigSecurityAuthorizeHttpRequests(authz);
            if (loginExclude.length > 0) {
                authz.requestMatchers(loginExclude).permitAll();
            }
            authz.requestMatchers("/admin/auth/**", "/admin/public/**").permitAll()
                    .requestMatchers("/admin/**", "/ureport/**").authenticated()
                    .anyRequest().permitAll();
        });


        http.sessionManagement(cfg -> {
            int maximumSessions = systemProperties.getMaximumSessions();
            log.info("设置最大并发会话数为 {}", maximumSessions);

            cfg.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
            cfg.sessionConcurrency(configurer -> {
                configurer.
                        maximumSessions(maximumSessions)
                        .maxSessionsPreventsLogin(true) // true:阻止新登录，false:踢出旧会话
                ;
            });

        });


        http.authenticationManager(authenticationConfiguration.getAuthenticationManager());

        // 认证通过后判断是否需要刷新权限（如修改用户）
        http.addFilterAfter(permissionRefreshFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(cfg -> {
            cfg.accessDeniedHandler((request, response, e) -> {
                ResponseTool.response(response, AjaxResult.FORBIDDEN);
            }).authenticationEntryPoint((request, response, e) -> {
                ResponseTool.response(response, AjaxResult.UNAUTHORIZED);
            });
        });


        http.headers(cfg -> cfg.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));   // iframe 允许同域名下访问， 如嵌入ureport报表
        http.csrf(AbstractHttpConfigurer::disable); // 前后端分离项目，关闭csrf
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);

        http.apply(new LoginConfigurer<>());


        return http.build();
    }


    // 密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordTool.getEncoder();
    }





    // 是 maximumSessions 控制的必要配置
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }


}
