package io.github.jiangood.openadmin.framework.data.converter;


import cn.hutool.core.util.StrUtil;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 字符串，数组
 */
public class ToListComplexConverter implements AttributeConverter<List<String>, String>, Serializable {


    public static final String CONJUNCTION = "::||,||::";
    private static final long serialVersionUID = 1L;

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null) {
            return null;
        }

        return StrUtil.join(CONJUNCTION, list);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.length() == 0) {
            return new ArrayList<>();
        }
        return StrUtil.split(dbData, CONJUNCTION);
    }

}
