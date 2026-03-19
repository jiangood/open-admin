package io.github.jiangood.openadmin.framework.config.security;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.security.refresh.PermissionRefreshFilter;
import io.github.jiangood.openadmin.framework.lifecycle.OpenLifecycle;
import io.github.jiangood.openadmin.lang.ArrayTool;
import io.github.jiangood.openadmin.lang.ResponseTool;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity  // 必须启用这个注解
public class SecurityConfig {

    private final SystemProperties systemProperties;

    private final Collection<OpenLifecycle> lifecycles;

    private final PermissionRefreshFilter permissionRefreshFilter;
    private final SecurityHolder securityHolder;

    public static final String[] LOGIN_EXCLUDE = {
            "/admin/auth/**", "/admin/public/**"
    };


    // 配置 HTTP 安全
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<String> exclude = new ArrayList<>(systemProperties.getLoginExcludePathPatterns());
        Collections.addAll(exclude, LOGIN_EXCLUDE);


        lifecycles.forEach(l -> l.onConfigSecurity(http));

        http.authorizeHttpRequests(authz -> {
            lifecycles.forEach(l -> l.onConfigSecurityAuthorizeHttpRequests(authz));
            authz.requestMatchers(ArrayTool.toStrArr(exclude)).permitAll()
                    .requestMatchers("/admin/**", "/ureport/**").authenticated()
                    .anyRequest().permitAll();
        });


        http.sessionManagement(cfg -> {
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
                ResponseTool.response(response, AjaxResult.UNAUTHORIZED);
            });
        });


        http.headers(cfg -> cfg.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));   // iframe 允许同域名下访问， 如嵌入ureport报表
        http.csrf(AbstractHttpConfigurer::disable); // 前后端分离项目，关闭csrf
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);

        DefaultSecurityFilterChain chain = http.build();

        // 缓存对象
        securityHolder.setSharedObjects(http.getSharedObjects());

        return chain;
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
