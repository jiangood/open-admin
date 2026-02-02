package io.github.jiangood.openadmin.framework.data.converter;


import io.github.jiangood.openadmin.lang.dto.Entry;
import io.github.jiangood.openadmin.lang.JsonTool;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 字符串，数组
 *
 * @author 姜涛
 * 使用方式：getter上加	@Convert(converter = StringArrayConverter.class)
 */
public class ToEntryListConverter implements AttributeConverter<List<Entry>, String>, Serializable {


    private static final long serialVersionUID = 1L;

    @Override
    public String convertToDatabaseColumn(List<Entry> list) {
        if (list == null) {
            return null;
        }

        return JsonTool.toJsonQuietly(list);
    }

    @Override
    public List<Entry> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return Collections.emptyList();
        }

        return JsonTool.jsonToBeanListQuietly(dbData, Entry.class);
    }

}
