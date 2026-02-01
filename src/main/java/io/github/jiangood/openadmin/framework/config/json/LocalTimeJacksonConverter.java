package io.github.jiangood.openadmin.framework.config.json;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 自定义Jackson反序列化日期类型时应用的类型转换器, 可接受多种前端出入得格式
 *
 * @author jiangtao
 */
public class LocalTimeJacksonConverter extends JsonDeserializer<LocalTime> {

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String originDate = p.getText();
        DateTime dateTime = DateUtil.parse(originDate);
        LocalDateTime localTime = LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());


        return localTime.toLocalTime();
    }


    @Override
    public Class<?> handledType() {
        return LocalTime.class;
    }
}
