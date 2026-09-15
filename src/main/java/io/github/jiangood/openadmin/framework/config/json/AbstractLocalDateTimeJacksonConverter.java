package io.github.jiangood.openadmin.framework.config.json;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Jackson日期类型反序列化器基类, 可接受多种前端输入格式
 *
 * @author jiangtao
 */
public abstract class AbstractLocalDateTimeJacksonConverter<T> extends ValueDeserializer<T> {

    protected LocalDateTime toLocalDateTime(JsonParser p, DeserializationContext ctxt) {
        DateTime dateTime = DateUtil.parse(p.getValueAsString());
        return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
    }

    protected abstract T convert(LocalDateTime localDateTime);

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) {
        return convert(toLocalDateTime(p, ctxt));
    }
}
