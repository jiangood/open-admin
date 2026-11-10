package io.github.jiangood.openadmin.framework.config.security;

import cn.hutool.core.collection.CollUtil;
import io.github.jiangood.openadmin.framework.auth.online.OnlineUserRegistry;
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
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
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

    private final SecurityHolder securityHolder;


    // 配置 HTTP 安全
    // 注意：PermissionRefreshFilter 必须作为方法参数注入，不能构造注入，
    // 否则会形成循环依赖：SecurityConfig -> PermissionRefreshFilter -> UserDetailsService -> SysUserService -> passwordEncoder(SecurityConfig)
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   PermissionRefreshFilter permissionRefreshFilter,
                                                   SessionRegistry sessionRegistry,
                                                   OnlineUserRegistry onlineUserRegistry) throws Exception {
        http.securityMatcher("/admin/**", "/file/**", "/ureport/**")
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
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers("/admin/public/**", "/admin/auth/**").permitAll()
                            .requestMatchers("/file/public/**").permitAll()
                            .requestMatchers("/file/**").authenticated()
                            .requestMatchers("/admin/**", "/ureport/**").authenticated();
                    if (CollUtil.isNotEmpty(systemProperties.getLoginExcludePathPatterns())) {
                        authz.requestMatchers(ArrayTool.toStrArr(systemProperties.getLoginExcludePathPatterns())).permitAll();
                    }
                })
                .sessionManagement(cfg -> {
                    int maximumSessions = systemProperties.getMaximumSessions();
                    boolean preventsLogin = systemProperties.isMaxSessionsPreventsLogin();
                    log.info("设置最大并发会话数为 {}，超出时{}", maximumSessions, preventsLogin ? "阻止新登录" : "踢出旧会话");
                    // 同域多套应用共用 JSESSIONID 会互相顶掉登录态，把实际生效的 cookie 配置打进启动日志便于对照
                    log.info("会话 cookie: name={} path={} timeout={} persistent={}",
                            environment.getProperty("server.servlet.session.cookie.name", "JSESSIONID"),
                            environment.getProperty("server.servlet.session.cookie.path",
                                    environment.getProperty("server.servlet.context-path", "/")),
                            environment.getProperty("server.servlet.session.timeout", "180m"),
                            environment.getProperty("server.servlet.session.persistent", "false"));

                    cfg.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                    cfg.sessionConcurrency(configurer -> {
                        configurer.maximumSessions(maximumSessions)
                                .maxSessionsPreventsLogin(preventsLogin) // true:阻止新登录，false:踢出旧会话
                                // 显式使用同一个 registry，在线用户列表与强制下线才能共享会话信息
                                .sessionRegistry(sessionRegistry)
                                // 被顶下线的请求返回 401 + JSON，而非 Spring Security 默认的 200 纯文本
                                .expiredSessionStrategy(new ConcurrentSessionExpiredStrategy(systemProperties));
                    });

                });


        // 认证通过后判断是否需要刷新权限（如修改用户）
        http.addFilterAfter(permissionRefreshFilter, UsernamePasswordAuthenticationFilter.class);

        // 应用重启后 Tomcat 会恢复 HttpSession，但内存态的会话注册表为空，这里按需补登记
        // 放在 ConcurrentSessionFilter 之前，保证同一请求内恢复的会话也能参与并发控制
        http.addFilterBefore(new SessionRestoreFilter(sessionRegistry, onlineUserRegistry), ConcurrentSessionFilter.class);


        http.exceptionHandling(cfg -> {
            cfg.accessDeniedHandler((request, response, e) -> ResponseTool.response(response, AjaxResult.FORBIDDEN))
                    .authenticationEntryPoint((request, response, e) -> {
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
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }



}
