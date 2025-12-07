package io.admin.framework.data.converter;


import cn.hutool.core.util.StrUtil;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 字符串，数组
 */
public class ToSetComplexConverter implements AttributeConverter<Set<String>, String>, Serializable {


    private static final long serialVersionUID = 1L;
    public static final String CONJUNCTION = "::||,||::";

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
        List<String> arr = StrUtil.split(dbData, CONJUNCTION);

        return new HashSet<>(arr);
    }

}
