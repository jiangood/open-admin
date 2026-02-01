package io.github.jiangood.openadmin.framework.data.converter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 区别于ToListConverter， 前后都增加中括号
 */
public class ToListBracketConverter implements AttributeConverter<List<String>, String>, Serializable {
    public static final String CONJUNCTION = ",";
    public static final String PREFIX = "[";
    public static final String SUFFIX = "]";
    private static final long serialVersionUID = 1L;

    public ToListBracketConverter() {
    }

    private static String join(List<String> list) {
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return CollUtil.join(list, CONJUNCTION, PREFIX, SUFFIX);
    }

    private static List<String> split(String dbData) {
        List<String> list = StrUtil.split(dbData, CONJUNCTION);

        for (int i = 0; i < list.size(); i++) {
            String item = list.get(i);
            item = item.substring(1, item.length() - 1);
            list.set(i, item);
        }

        return list;
    }

    public String convertToDatabaseColumn(List<String> list) {
        return list == null ? null : join(list);
    }

    public List<String> convertToEntityAttribute(String dbData) {
        return StrUtil.isNotEmpty(dbData) ? split(dbData) : Collections.emptyList();
    }


}
