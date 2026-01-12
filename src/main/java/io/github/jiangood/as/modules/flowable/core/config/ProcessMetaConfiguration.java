package io.github.jiangood.as.modules.flowable.core.config;


import io.github.jiangood.as.modules.flowable.core.config.meta.ProcessMeta;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * 这个类存在的意义只是提供代码提示，真正的解析类为 ProcessMetaDaoYmlImpl
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "process")
public class ProcessMetaConfiguration {

    private List<ProcessMeta> list;

}




