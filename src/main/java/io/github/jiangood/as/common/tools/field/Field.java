package io.github.jiangood.as.common.tools.field;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Field {

    String label;
    String name;

    boolean required;

    String defaultValue;

    String valueType;

    Map<String, Object> componentProps;


}
