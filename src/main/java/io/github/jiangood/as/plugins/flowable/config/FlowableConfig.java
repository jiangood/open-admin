package io.github.jiangood.as.plugins.flowable.config;

import io.github.jiangood.as.common.tools.IdTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;

import java.util.ArrayList;

@ConditionalOnClass(org.flowable.spring.SpringProcessEngineConfiguration.class)
@Slf4j
@AutoConfiguration
@AllArgsConstructor
@ComponentScan
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {


    private GlobalProcessListener globalProcessListener;


    @Override
    public void configure(SpringProcessEngineConfiguration cfg) {
        // 主键生成器，注意：不会影响act_de开头的表主键生成，因为这是流程设计器的，不是工作流引擎的
        cfg.setIdGenerator(IdTool::uuidV7);
        if (cfg.getEventListeners() == null) {
            cfg.setEventListeners(new ArrayList<>());
        }
        cfg.getEventListeners().add(globalProcessListener);
    }


}
