package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.framework.config.SysProperties;
import io.github.jiangood.openadmin.framework.config.init.SystemHookService;
import io.github.jiangood.openadmin.framework.config.security.refresh.PermissionRefreshFilter;
import io.github.jiangood.openadmin.lang.ArrayTool;
import io.github.jiangood.openadmin.lang.PasswordTool;
import io.github.jiangood.openadmin.lang.ResponseTool;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity  // 必须启用这个注解
public class SecurityConfig {

    private final SysProperties sysProperties;

    private final SystemHookService systemHookService;


    // 配置 HTTP 安全
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,  PermissionRefreshFilter permissionRefreshFilter) throws Exception {
        String[] loginExclude = ArrayTool.toStrArr(sysProperties.getXssExcludePathList());

        systemHookService.beforeConfigSecurity(http);

        http.authorizeHttpRequests(authz -> {
            if (loginExclude.length > 0) {
                authz.requestMatchers(loginExclude).permitAll();
            }
            authz.requestMatchers("/admin/auth/**", "/admin/public/**").permitAll()
                    .requestMatchers("/admin/**", "/ureport/**").authenticated()
                    .anyRequest().permitAll();
        });


        http.sessionManagement(cfg -> {
            int maximumSessions = sysProperties.getMaximumSessions();
            log.info("设置最大并发会话数为 {}", maximumSessions);

            cfg.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
            cfg.sessionConcurrency(configurer -> {
                configurer.
                        maximumSessions(maximumSessions)
                        .maxSessionsPreventsLogin(true) // true:阻止新登录，false:踢出旧会话
                        .sessionRegistry(sessionRegistry());
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


        return http.build();

    }


    // 密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordTool.getEncoder();
    }


    @Bean
    public AuthenticationManager authManager(List<AuthenticationProvider> providers) {
        return new ProviderManager(providers);
    }

    @Bean
    @ConditionalOnMissingBean // 这样用户可以自定义redisSession等来覆盖
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * **重要**：注册一个 SessionRegistry Bean
     * SessionRegistry 用于记录和管理所有的 Session 信息，是 maximumSessions 控制的基础。
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        // 使用标准的 SessionRegistryImpl 即可
        return new SessionRegistryImpl();
    }

    // 是 maximumSessions 控制的必要配置
    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }
}
