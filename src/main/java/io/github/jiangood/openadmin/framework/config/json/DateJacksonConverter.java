package io.github.jiangood.openadmin.framework.config.json;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

/**
 * 自定义Jackson反序列化日期类型时应用的类型转换器, 可接受多种前端出入得格式
 *
 * @author jiangtao
 */
public class DateJacksonConverter extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String originDate = p.getText();

        return DateUtil.parse(originDate);
    }

    @Override
    public Class<?> handledType() {
        return Date.class;
    }
}
