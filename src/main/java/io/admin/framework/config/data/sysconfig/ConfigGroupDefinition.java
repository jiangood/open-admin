package io.admin.framework.config.data.sysconfig;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConfigGroupDefinition {
    String groupName;
    List<ConfigDefinition> children = new ArrayList<>();
}
