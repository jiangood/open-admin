package io.github.jiangood.openadmin.framework.config.json;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeJacksonConverter extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String originDate = p.getText();
        DateTime dateTime = DateUtil.parse(originDate);


        return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
    }


    @Override
    public Class<?> handledType() {
        return LocalDateTime.class;
    }
}
