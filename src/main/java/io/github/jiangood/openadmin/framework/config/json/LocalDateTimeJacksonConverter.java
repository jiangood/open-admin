package io.github.jiangood.openadmin.framework.config.json;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;
import java.time.ZoneId;

@JacksonComponent
public class LocalDateTimeJacksonConverter extends ValueDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) {
        String originDate = p.getValueAsString();
        DateTime dateTime = DateUtil.parse(originDate);


        return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
    }

}
