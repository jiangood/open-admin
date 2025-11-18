package io.admin.framework.config.data;

import io.admin.framework.config.data.sysconfig.ConfigGroupDefinition;
import io.admin.framework.config.data.sysmenu.MenuDefinition;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "data")
@Data
 class DataPropConfig {

    private List<MenuDefinition> menus = new ArrayList<>();


    private List<ConfigGroupDefinition> configs = new ArrayList<>();
}
