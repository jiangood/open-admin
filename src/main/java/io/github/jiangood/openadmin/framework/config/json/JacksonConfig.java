package io.github.jiangood.openadmin.framework.config.json;

import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 * jackson 的配置
 * 自定义一些关于时间的 格式
 *
 * @author jiangtao
 */
@Configuration
public class JacksonConfig {


    /**
     * json自定义序列化工具,long转string
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {

            // 注册 Java 8 时间模块
            builder.modules(new JavaTimeModule());


            builder.serializerByType(Long.class, new LongToStringSerializer())
                    .serializerByType(Long.TYPE, new LongToStringSerializer())

                    // 日期 -> 字符串
                    .serializerByType(Date.class, new DateSerializer(false, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")))
                    .serializerByType(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .serializerByType(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                    .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))


                    // 字符串 -> 日期
                    .deserializerByType(Date.class, new DateJacksonConverter())
                    .deserializerByType(LocalDate.class, new LocalDateJacksonConverter())
                    .deserializerByType(LocalTime.class, new LocalTimeJacksonConverter())
                    .deserializerByType(LocalDateTime.class, new LocalDateTimeJacksonConverter());


            builder.serializerByType(Page.class, new PageJsonSerializer());
        };
    }


}


