package io.github.jiangood.openadmin.framework.config.json;

import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

@JacksonComponent
public class LongToStringSerializer extends ValueSerializer<Long> {

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializationContext ctx) {
        gen.writeString(value.toString());
    }
}
