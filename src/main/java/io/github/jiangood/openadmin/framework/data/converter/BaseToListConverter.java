package io.github.jiangood.openadmin.framework.data.converter;


import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.lang.ReflectTool;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaseToListConverter<T> implements AttributeConverter<List<T>, String>, Serializable {


    private static final long serialVersionUID = 1L;


    @Override
    public String convertToDatabaseColumn(List<T> input) {
        return JsonTool.toJsonQuietly(input);
    }

    @Override
    public List<T> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }

        Class<T> cls = ReflectTool.getClassGenricType(getClass());

        return JsonTool.jsonToBeanListQuietly(dbData, cls);
    }


}
