package io.github.jiangood.openadmin.framework.config.json;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 自定义Jackson反序列化日期类型时应用的类型转换器, 可接受多种前端出入得格式
 *
 * @author jiangtao
 */
@JacksonComponent
public class LocalDateJacksonConverter extends ValueDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) {
        String originDate = p.getValueAsString();
        DateTime dateTime = DateUtil.parse(originDate);

        LocalDateTime localTime = LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());

        return localTime.toLocalDate();
    }

}
