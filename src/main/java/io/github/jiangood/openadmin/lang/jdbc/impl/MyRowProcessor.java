package io.github.jiangood.openadmin.lang.jdbc.impl;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class MyRowProcessor extends BasicRowProcessor {

    boolean namingStrategyImproved;

    public MyRowProcessor(BeanProcessor convert, boolean namingStrategyImproved) {
        super(convert);
        this.namingStrategyImproved = namingStrategyImproved;
    }

    @Override
    public Map<String, Object> toMap(ResultSet resultSet) throws SQLException {
        Map<String, Object> map = super.toMap(resultSet);

        if (namingStrategyImproved) {
            Map<String, Object> result = BasicRowProcessor.createCaseInsensitiveHashMap(map.size());
            for (Map.Entry<String, Object> e : map.entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                key = StrUtil.toCamelCase(key);
                result.put(key, value);
            }
            return result;
        }
        return map;
    }
}
