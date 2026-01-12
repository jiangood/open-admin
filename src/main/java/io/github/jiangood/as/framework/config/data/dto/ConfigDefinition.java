package io.github.jiangood.as.framework.config.data.dto;


import io.github.jiangood.as.common.tools.field.ValueType;
import lombok.Data;

@Data
public class ConfigDefinition {
    String code;
    ValueType valueType;
    String description;
    String name;
}
