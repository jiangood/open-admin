package io.github.jiangood.openadmin.framework.config;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import io.github.jiangood.openadmin.framework.config.argument.resolver.RequestBodyKeysArgumentResolver;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * web配置
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableCaching
@ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
public class WebMvcWebMvcConfiguration implements WebMvcConfigurer {
    /**
     * 放开权限校验的接口
     */
    private static final String[] NONE_SECURITY_URL_PATTERNS = {
            // 前端静态文件
            "/**.jpg",
            "/**.png",
            "/static/**.jpg",
            "/static/**.png",
            "/favicon.ico",
            "/web/**",


            //后端的
            "/",
            "/login",
            "/logout",
            "/oauth/**",
            "/error",

            // 接口
            "/openApi/gateway/**"
    };
    @Resource
    SystemProperties systemProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //  registry.addInterceptor(appApiInterceptor).addPathPatterns(WebConstants.APP_API_PATTERN);

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String[] list = {
                "classpath:/META-INF/resources/",
                "classpath:/resources/",
                "classpath:/static/",
                "classpath:/public/",

                // 同级目录下的静态文件
                "file:./static/"
        };

        registry.addResourceHandler("/**").addResourceLocations(list);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RequestBodyKeysArgumentResolver());
    }

    /**
     * 由于引入了 jackson-dataformat-xml， 导致浏览器打开接口时返回xml(浏览器请求头Accept含xml)，这里设置默认返回json
     *
     * @param configurer
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.ignoreAcceptHeader(true)
                .defaultContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 验证码生成器
     */
    @Bean
    public CodeGenerator getCodeGenerator() {
        SystemProperties.CaptchaType captchaType = systemProperties.getCaptchaType();
        if (captchaType != null) {
            switch (captchaType) {
                case MATH -> {
                    return new MathGenerator(2);
                }
                case RANDOM -> {
                    return new RandomGenerator(4);
                }
            }
        }
        return new RandomGenerator(4);
    }
}
