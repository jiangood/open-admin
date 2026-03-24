package io.github.jiangood.openadmin.framework.config.security;

import cn.hutool.core.collection.CollUtil;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.config.security.refresh.PermissionRefreshFilter;
import io.github.jiangood.openadmin.lang.ArrayTool;
import io.github.jiangood.openadmin.lang.ResponseTool;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.modules.api.OpenApiFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

    private final PermissionRefreshFilter permissionRefreshFilter;
    private final SecurityHolder securityHolder;
    private final OpenApiFilter openApiFilter;


    // 配置 HTTP 安全
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**", "/ureport/**")
                .headers(cfg -> cfg.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))   // iframe 允许同域名下访问， 如嵌入ureport报表
                .csrf(AbstractHttpConfigurer::disable) // 前后端分离项目，关闭csrf
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers("/admin/public/**", "/admin/auth/**").permitAll()
                            .requestMatchers("/admin/**",
                                    // 报表
                                    "/ureport/**",
                                    // 接口文档 springdoc的默认地址，以免暴露
                                    "/swagger-ui/**",
                                    "/v3/api-docs").authenticated();
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
                ResponseTool.response(response, AjaxResult.UNAUTHORIZED);
            });
        });


        DefaultSecurityFilterChain chain = http.build();

        // 缓存对象
        securityHolder.setSharedObjects(http.getSharedObjects());

        return chain;
    }

    /**
     * 配置 2：业务 API 接口 (/api/**)
     * 允许跨域。
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**") // 只感应 /api 开头的请求
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(apiCorsSource())) // 开启跨域
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // 演示放行，可按需修改

        // 开放接口过滤器
        http.addFilterBefore(openApiFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 配置 3：公共资源与文档 (Swagger/静态资源)
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


    @Bean
    public CorsConfigurationSource apiCorsSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的源：正式环境建议指定域名，如 "https://www.yourdomain.com"
        config.setAllowedOriginPatterns(List.of("*"));

        // 允许的 HTTP 方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允许的 Header
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));

        // 是否允许发送 Cookie
        config.setAllowCredentials(true);

        // 预检请求（OPTIONS）的缓存时间（秒），避免频繁发送预检
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
