package io.github.jiangood.openadmin.framework.config;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
public class WebMvcConfiguration implements WebMvcConfigurer {
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

}
