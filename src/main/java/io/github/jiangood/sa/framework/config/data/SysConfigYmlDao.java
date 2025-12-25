package io.github.jiangood.sa.framework.config.data;

import io.github.jiangood.sa.common.tools.ResourceTool;
import io.github.jiangood.sa.common.tools.YmlTool;
import io.github.jiangood.sa.framework.config.data.dto.ConfigGroupDefinition;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class SysConfigYmlDao {
    private static final String MENU_CONFIG_PATTERN = "config/application-data*.yml";
    private final List<ConfigGroupDefinition> configs = new ArrayList<>();



    public List<ConfigGroupDefinition> findAll() {
        return Collections.unmodifiableList(configs);
    }


    @PostConstruct
    public void init() throws IOException {
        String[] ymls = ResourceTool.readAll(MENU_CONFIG_PATTERN);
        for (String yml : ymls) {
            DataProperties cur = YmlTool.parseYml(yml, DataProperties.class, "data");
            configs.addAll(cur.getConfigs());
        }
    }





}
