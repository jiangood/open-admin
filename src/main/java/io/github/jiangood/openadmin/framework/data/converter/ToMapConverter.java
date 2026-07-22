package io.github.jiangood.openadmin.framework.data.converter;

import java.util.Collections;
import java.util.Map;

public class ToMapConverter extends BaseConverter<Map<String, String>> {

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        Map<String, String> result = super.convertToEntityAttribute(dbData);
        return result != null ? result : Collections.emptyMap();
    }
}
