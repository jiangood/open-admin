package io.github.jiangood.openadmin.framework.data.converter;

import io.github.jiangood.openadmin.lang.URLTool;
import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// 转换为 a=1&b=2这种格式
public class ToQueryStringMapConverter implements AttributeConverter<Map<String, Object>, String>, Serializable {


    @Override
    public String convertToDatabaseColumn(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue());
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }


    @Override
    public Map<String, Object> convertToEntityAttribute(String queryString) {
        Map<String, String> map = URLTool.getParams(queryString);
        return new HashMap<>(map);
    }
}
