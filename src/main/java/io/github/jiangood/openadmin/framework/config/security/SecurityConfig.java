package io.github.jiangood.openadmin.framework.config.security;

import cn.hutool.core.collection.CollUtil;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.util.ArrayTool;
import io.github.jiangood.openadmin.util.ResponseTool;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity  // 必须启用这个注解
public class SecurityConfig {


    private final SystemProperties systemProperties;
    private final Environment environment;

    private final PermissionRefreshFilter permissionRefreshFilter;
    private final SecurityHolder securityHolder;


    // 配置 HTTP 安全
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**", "/ureport/**")
                .headers(cfg -> cfg
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)   // iframe 允许同域名下访问，如嵌入ureport报表
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data: blob:; " +
                                "font-src 'self' data:; " +
                                "connect-src 'self' ws:"
                        ))
                )   // X-Content-Type-Options/Cache-Control 由 Spring Security 自动添加
                .csrf(AbstractHttpConfigurer::disable) // SPA 前后端分离 + Token 在请求体传输，天然免疫 CSRF；若改为传统 Form-Cookie 渲染需重新启用
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers("/admin/public/**", "/admin/auth/**").permitAll()
                            .requestMatchers("/admin/**", "/ureport/**").authenticated();
                    if (CollUtil.isNotEmpty(systemProperties.getLoginExcludePathPatterns())) {
                        authz.requestMatchers(ArrayTool.toStrArr(systemProperties.getLoginExcludePathPatterns())).permitAll();
                    }
                })
                .sessionManagement(cfg -> {
                    int maximumSessions = systemProperties.getMaximumSessions();
                    log.info("设置最大并发会话数为 {}", maximumSessions);

                    cfg.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                    cfg.sessionConcurrency(configurer -> {
                        configurer.maximumSessions(maximumSessions)
                                .maxSessionsPreventsLogin(false) // true:阻止新登录，false:踢出旧会话
                        ;
                    });

                });


        // 认证通过后判断是否需要刷新权限（如修改用户）
        http.addFilterAfter(permissionRefreshFilter, UsernamePasswordAuthenticationFilter.class);


        http.exceptionHandling(cfg -> {
            cfg.accessDeniedHandler((request, response, e) -> {
                ResponseTool.response(response, AjaxResult.FORBIDDEN);
            }).authenticationEntryPoint((request, response, e) -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                ResponseTool.response(response, AjaxResult.UNAUTHORIZED);
            });
        });


        DefaultSecurityFilterChain chain = http.build();

        // 缓存对象
        securityHolder.setSharedObjects(http.getSharedObjects());

        return chain;
    }



    /**
     * 配置 3：公共资源
     * 兜底配置。
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
        );
        return http.build();
    }


    // 密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }



}
