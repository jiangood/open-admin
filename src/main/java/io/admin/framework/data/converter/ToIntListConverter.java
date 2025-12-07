package io.admin.framework.data.converter;


import cn.hutool.core.util.StrUtil;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ToIntListConverter implements AttributeConverter<List<Integer>, String>, Serializable {


    private static final long serialVersionUID = 1L;
    public static final String CONJUNCTION = ",";

    @Override
    public String convertToDatabaseColumn(List<Integer> list) {
        if (list == null) {
            return null;
        }

        return StrUtil.join(CONJUNCTION, list);
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.length() == 0) {
            return new ArrayList<>();
        }
        List<String> list = StrUtil.split(dbData, CONJUNCTION);

        return list.stream().map(Integer::parseInt).collect(Collectors.toList());
    }

}
