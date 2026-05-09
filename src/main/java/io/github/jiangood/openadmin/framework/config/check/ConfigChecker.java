package io.github.jiangood.openadmin.framework.config.check;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigChecker implements ApplicationRunner {
    
    @Value("${spring.jackson.time-zone:}")
    private String timeZone;
    
    @Value("${spring.jackson.date-format:}")
    private String dateFormat;
    
    @Value("${spring.jackson.locale:}")
    private String locale;
    
    @Value("${spring.data.web.pageable.one-indexed-parameters:}")
    private Boolean oneIndexed;
    
    @Override
    public void run(ApplicationArguments args) {
        // 期望的配置值
        String expectedTimeZone = "GMT+8";
        String expectedDateFormat = "yyyy-MM-dd HH:mm:ss";
        String expectedLocale = "zh_CN";
        boolean expectedOneIndexed = true;
        
        // 一次性检查所有配置
        if (!expectedTimeZone.equals(timeZone) ||
            !expectedDateFormat.equals(dateFormat) ||
            !expectedLocale.equals(locale) ||
            expectedOneIndexed != oneIndexed) {
            
            log.error("配置错误！期望配置：\n  spring.jackson.time-zone: {} (当前: {})\n  spring.jackson.date-format: {} (当前: {})\n  spring.jackson.locale: {} (当前: {})\n  spring.data.web.pageable.one-indexed-parameters: {} (当前: {})",
                expectedTimeZone, timeZone, expectedDateFormat, dateFormat, expectedLocale, locale, expectedOneIndexed, oneIndexed);
            
            // 可以选择抛出异常阻止启动，或者只打印警告
            throw new RuntimeException("配置校验失败，请检查 application.yml");
        }
        
        log.info("配置校验通过，项目启动成功！");
    }
}