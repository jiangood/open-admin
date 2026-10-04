package io.github.jiangood.openadmin.framework.config.json;

import org.springframework.boot.jackson.JacksonComponent;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 自定义Jackson反序列化日期类型时应用的类型转换器, 可接受多种前端输入格式
 *
 * @author jiangtao
 */
@JacksonComponent
public class LocalDateJacksonConverter extends AbstractLocalDateTimeJacksonConverter<LocalDate> {

    @Override
    protected LocalDate convert(LocalDateTime localDateTime) {
        return localDateTime.toLocalDate();
    }
}
