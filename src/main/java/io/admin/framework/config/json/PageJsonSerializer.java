package io.admin.framework.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.admin.framework.data.domain.PageExt;
import org.springframework.data.domain.Page;

import java.io.IOException;

/***
 * 前后端交互式，分页从1开始
 *
 *
 * @gendoc
 */
public class PageJsonSerializer<T> extends JsonSerializer<Page<T>> {

    /**
     * 前后端交互时，分页是否从1开始的算的
     */
    private static final boolean oneIndexed = true;

    @Override
    public void serialize(Page<T> page, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        int number = page.getNumber();
        if (oneIndexed) {
            number++;
        }

        gen.writeStartObject(); // means start, like '{}'

        gen.writeNumberField("page", number); // 页码
        gen.writeNumberField("size", page.getSize()); //每页大小

        gen.writeObjectField("content", page.getContent());

        gen.writeBooleanField("empty", page.isEmpty());
        gen.writeBooleanField("first", page.isFirst());
        gen.writeBooleanField("last", page.isLast());

        gen.writeNumberField("number", number);
        gen.writeNumberField("numberOfElements", page.getNumberOfElements());
        gen.writeNumberField("totalPages", page.getTotalPages());
        gen.writeNumberField("totalElements", page.getTotalElements());

        if (page instanceof PageExt<T> ext) {
            gen.writeObjectField("extData", ext.getExtData());
        }

        gen.writeEndObject();
    }
}
