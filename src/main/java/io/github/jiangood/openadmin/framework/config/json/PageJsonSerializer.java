package io.github.jiangood.openadmin.framework.config.json;

import io.github.jiangood.openadmin.framework.data.PageExt;
import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.data.domain.Page;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;


/***
 * 前后端交互式，分页从1开始
 *
 *
 * @gendoc
 */
@JacksonComponent
public class PageJsonSerializer<T> extends ValueSerializer<Page<T>> {

    /**
     * 前后端交互时，分页是否从1开始的算的
     */
    private static final boolean oneIndexed = true;

    @Override
    public void serialize(Page<T> page, JsonGenerator gen, SerializationContext ctx) {
        int number = page.getNumber();
        if (oneIndexed) {
            number++;
        }

        gen.writeStartObject();

        gen.writeNumberProperty("page", number);
        gen.writeNumberProperty("size", page.getSize());

        gen.writePOJOProperty("content", page.getContent());

        gen.writeBooleanProperty("empty", page.isEmpty());
        gen.writeBooleanProperty("first", page.isFirst());
        gen.writeBooleanProperty("last", page.isLast());

        gen.writeNumberProperty("number", number);
        gen.writeNumberProperty("numberOfElements", page.getNumberOfElements());
        gen.writeNumberProperty("totalPages", page.getTotalPages());
        gen.writeNumberProperty("totalElements", page.getTotalElements());

        if (page instanceof PageExt<T> ext) {
            gen.writePOJOProperty("extData", ext.getExtData());
        }

        gen.writeEndObject();
    }
}
