package io.github.jiangood.openadmin.framework.data.converter;


import tools.jackson.core.type.TypeReference;
import io.github.jiangood.openadmin.util.JsonTool;
import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;

@Slf4j
public class BaseConverter<T> implements AttributeConverter<T, String>, Serializable {


    private static final long serialVersionUID = 1L;

    @Override
    public String convertToDatabaseColumn(T input) {
        // hutool的数组有bug
        return JsonTool.toJsonQuietly(input);
    }


    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        // 兼容性处理
        if ("[]".equals(dbData)) {
            return null;
        }


        TypeReference<T> reference = new TypeReference<T>() {
        };
        try {
            return JsonTool.jsonToBean(dbData, reference);
        } catch (IOException e) {
            log.error("JSON转换实体失败", e);
        }
        return null;
    }

}
