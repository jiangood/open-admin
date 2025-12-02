package io.admin.modules.flowable.core.config;

// ... imports

import io.admin.common.utils.YmlUtils;
import io.admin.modules.flowable.core.definition.ProcessDefinition;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "process")
public class ProcessConfiguration {

    private List<ProcessDefinition> definitions;


    @PostConstruct
    void init() throws IOException {
        ProcessConfiguration cfg = YmlUtils.parseYml("classpath:application-process.yml", ProcessConfiguration.class,"process");
        this.definitions = cfg.getDefinitions();
    }
}




