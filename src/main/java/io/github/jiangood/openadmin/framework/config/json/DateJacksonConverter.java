package io.github.jiangood.openadmin.framework.config.json;

import cn.hutool.core.date.DateUtil;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.io.IOException;
import java.util.Date;

/**
 * 自定义Jackson反序列化日期类型时应用的类型转换器, 可接受多种前端出入得格式
 *
 * @author jiangtao
 */
@JacksonComponent
public class DateJacksonConverter extends ValueDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) {
        String originDate = p.getText();

        return DateUtil.parse(originDate);
    }

}
