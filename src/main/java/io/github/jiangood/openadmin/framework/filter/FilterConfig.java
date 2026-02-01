package io.github.jiangood.openadmin.framework.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<CachingJsonRequestBodyFilter> cachingRequestBodyFilterRegistrationBean(CachingJsonRequestBodyFilter filter) {

        FilterRegistrationBean<CachingJsonRequestBodyFilter> registration = new FilterRegistrationBean<>(filter);

        registration.addUrlPatterns("/*");
        return registration;
    }
}
