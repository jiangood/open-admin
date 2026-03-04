package io.github.jiangood.openadmin.framework.config.check;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
            
            System.err.println("❌ 配置错误！期望配置：");
            System.err.println("  spring.jackson.time-zone: " + expectedTimeZone + " (当前: " + timeZone + ")");
            System.err.println("  spring.jackson.date-format: " + expectedDateFormat + " (当前: " + dateFormat + ")");
            System.err.println("  spring.jackson.locale: " + expectedLocale + " (当前: " + locale + ")");
            System.err.println("  spring.data.web.pageable.one-indexed-parameters: " + expectedOneIndexed + " (当前: " + oneIndexed + ")");
            
            // 可以选择抛出异常阻止启动，或者只打印警告
            throw new RuntimeException("配置校验失败，请检查 application.yml");
        }
        
        System.out.println("✅ 配置校验通过，项目启动成功！");
    }
}