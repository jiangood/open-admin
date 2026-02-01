package io.github.jiangood.openadmin.framework.data.converter;


import cn.hutool.core.util.StrUtil;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 字符串，数组
 *
 * @author 姜涛
 * 使用方式：getter上加	@Convert(converter = StringArrayConverter.class)
 */
public class ToSetConverter implements AttributeConverter<Set<String>, String>, Serializable {


    public static final String CONJUNCTION = ",";
    private static final long serialVersionUID = 1L;

    @Override
    public String convertToDatabaseColumn(Set<String> list) {
        if (list == null) {
            return null;
        }

        return StrUtil.join(CONJUNCTION, list);
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.length() == 0) {
            return new HashSet<>();
        }
        return new HashSet<>(StrUtil.split(dbData, CONJUNCTION));
    }

}
