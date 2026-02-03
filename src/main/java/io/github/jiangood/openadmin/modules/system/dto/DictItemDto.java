package io.github.jiangood.openadmin.modules.system.dto;

import io.github.jiangood.openadmin.framework.enums.StatusColor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DictItemDto {
    private Object value;
    private String label;
    private String color;
}