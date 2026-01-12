package io.github.jiangood.as.common.tools.jdbc.impl;

import io.github.jiangood.as.common.tools.ConvertTool;
import io.github.jiangood.as.common.tools.ReflectionTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.GenerousBeanProcessor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;
import java.sql.SQLException;

@Slf4j
public class MyBeanProcessor extends GenerousBeanProcessor {


    @Override
    protected void callSetter(Object target, PropertyDescriptor prop, Object value) throws SQLException {
        value = this.convertValue(target, prop, value);

        super.callSetter(target, prop, value);
    }

    private Object convertValue(Object target, PropertyDescriptor prop, Object value) {
        if (value == null) {
            return null;
        }
        Class<?> type = prop.getPropertyType();
        if (type.isAssignableFrom(value.getClass())) {
            return value;
        }

        Type firstGeneric = ReflectionTool.getFirstGeneric(prop);

        try {
            Object value2 = ConvertTool.convert(type, value, firstGeneric);
            return value2;
        } catch (Exception e) {
            log.warn("数据类型不匹配: {}无法转换为{}, 请修改字段类型", value.getClass().getSimpleName(), type.getSimpleName());
        }
        return target;
    }


}
