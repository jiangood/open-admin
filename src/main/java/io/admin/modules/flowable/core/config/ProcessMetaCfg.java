package io.admin.modules.flowable.core.config;

// ... imports

import io.admin.common.utils.YmlUtils;
import io.admin.modules.flowable.core.definition.ProcessMeta;
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
public class ProcessMetaCfg {

    private List<ProcessMeta> list;


    @PostConstruct
    void init() throws IOException {
        ProcessMetaCfg cfg = YmlUtils.parseYml("classpath:application-process.yml", ProcessMetaCfg.class,"process");
        this.list = cfg.getList();
    }


    public ProcessMeta getMeta(String key) {
        return this.list.stream().filter(item -> item.getKey().equals(key)).findFirst().orElse(null);
    }
}




