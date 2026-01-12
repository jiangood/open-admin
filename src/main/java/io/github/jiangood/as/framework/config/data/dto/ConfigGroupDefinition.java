package io.github.jiangood.as.framework.config.data.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConfigGroupDefinition {
    String groupName;
    List<ConfigDefinition> children = new ArrayList<>();
}
