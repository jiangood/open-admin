package io.github.jiangood.openadmin.framework.config.json;

import org.springframework.boot.jackson.JacksonComponent;

import java.time.LocalDateTime;

@JacksonComponent
public class LocalDateTimeJacksonConverter extends AbstractLocalDateTimeJacksonConverter<LocalDateTime> {

    @Override
    protected LocalDateTime convert(LocalDateTime localDateTime) {
        return localDateTime;
    }
}
