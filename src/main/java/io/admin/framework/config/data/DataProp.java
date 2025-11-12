package io.admin.framework.config.data;

import io.admin.framework.config.data.sysconfig.ConfigDefinition;
import io.admin.framework.config.data.sysconfig.ConfigGroupDefinition;
import io.admin.framework.config.data.sysmenu.MenuBadge;
import io.admin.framework.config.data.sysmenu.MenuDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "data")
@Data
public class DataProp {

    private List<MenuDefinition> menus = new ArrayList<>();

    private List<MenuBadge> badges = new ArrayList<>();

    private List<ConfigGroupDefinition> configs = new ArrayList<>();
}
