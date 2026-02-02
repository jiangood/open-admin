package io.github.jiangood.openadmin.framework.config.security;

import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.ArrayTool;
import io.github.jiangood.openadmin.lang.PasswordTool;
import io.github.jiangood.openadmin.lang.ResponseTool;
import io.github.jiangood.openadmin.framework.config.SysProperties;
import io.github.jiangood.openadmin.framework.config.init.SystemHookService;
import io.github.jiangood.openadmin.framework.config.security.refresh.PermissionRefreshFilter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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

@Slf4j
@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity  // 必须启用这个注解
public class SecurityConfig {

    private final SysProperties sysProperties;

    private SystemHookService systemHookService;

    // 配置 HTTP 安全
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginFilter loginFilter, PermissionRefreshFilter permissionRefreshFilter) throws Exception {
        String[] loginExclude = ArrayTool.toStrArr(sysProperties.getXssExcludePathList());

        systemHookService.beforeConfigSecurity(http);
        // 前后端分离项目，关闭csrf
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authz -> {
            if (loginExclude.length > 0) {
                authz.requestMatchers(loginExclude).permitAll();
            }
            authz.requestMatchers("/admin/auth/**", "/admin/public/**").permitAll()
                    .requestMatchers("/admin/**", "/ureport/**").authenticated()
                    .anyRequest().permitAll();
        });

        http.formLogin(cfg -> {
            cfg.loginProcessingUrl("/admin/auth/login").defaultSuccessUrl("/admin/auth/success").successHandler((request, response, authentication) -> {
                AjaxResult rs = AjaxResult.ok("登录成功");
                ResponseTool.response(response, rs);
            }).failureHandler((request, response, exception) -> {
                AjaxResult rs = AjaxResult.err("登录失败" + exception.getMessage());
                ResponseTool.response(response, rs);
            });
        });

        http.sessionManagement(cfg -> {
            int maximumSessions = sysProperties.getMaximumSessions();
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
                ResponseTool.response(response, AjaxResult.FORBIDDEN);
            }).authenticationEntryPoint((request, response, e) -> {
                ResponseTool.response(response, AjaxResult.UNAUTHORIZED);
            });
        });

        // iframe 允许同域名下访问， 如嵌入ureport报表
        http.headers(cfg -> cfg.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));


        return http.build();

    }


    // 密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordTool.getPasswordEncoder();
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
