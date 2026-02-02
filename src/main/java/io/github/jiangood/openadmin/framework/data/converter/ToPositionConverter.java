package io.github.jiangood.openadmin.framework.data.converter;


import io.github.jiangood.openadmin.lang.dto.Position;
import io.github.jiangood.openadmin.lang.JsonTool;

import java.io.IOException;

public class ToPositionConverter extends BaseConverter<Position> {

    @Override
    public Position convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            try {
                return JsonTool.jsonToBean(dbData, Position.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }
}
