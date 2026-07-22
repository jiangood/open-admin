package io.github.jiangood.openadmin.util.field;

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
