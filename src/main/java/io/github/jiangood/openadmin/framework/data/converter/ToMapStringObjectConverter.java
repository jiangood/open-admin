package io.github.jiangood.openadmin.framework.data.converter;

import java.util.Collections;
import java.util.Map;

public class ToMapStringObjectConverter extends BaseConverter<Map<String, Object>> {

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        Map<String, Object> result = super.convertToEntityAttribute(dbData);
        return result != null ? result : Collections.emptyMap();
    }
}
