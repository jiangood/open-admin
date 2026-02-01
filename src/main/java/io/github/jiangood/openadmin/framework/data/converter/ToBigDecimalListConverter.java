package io.github.jiangood.openadmin.framework.data.converter;


import cn.hutool.core.util.StrUtil;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ToBigDecimalListConverter implements AttributeConverter<List<BigDecimal>, String>, Serializable {


    private static final long serialVersionUID = 1L;

    @Override
    public String convertToDatabaseColumn(List<BigDecimal> list) {
        if (list == null) {
            return null;
        }

        return StrUtil.join(",", list);
    }

    @Override
    public List<BigDecimal> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.length() == 0) {
            return new ArrayList<>();
        }

        List<String> list = StrUtil.split(dbData, ",");
        return list.stream().map(BigDecimal::new).collect(Collectors.toList());
    }

}
