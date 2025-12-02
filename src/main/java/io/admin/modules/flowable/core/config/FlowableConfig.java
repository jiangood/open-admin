
package io.admin.modules.flowable.core.config;

import io.admin.common.utils.IdTool;
import io.admin.common.utils.SpringUtils;
import io.admin.framework.config.init.SystemHookEventType;
import io.admin.framework.config.init.SystemHookService;
import io.admin.modules.flowable.admin.entity.SysFlowableModel;
import io.admin.modules.flowable.admin.service.SysFlowableModelService;
import io.admin.modules.flowable.core.definition.ProcessDefinition;
import io.admin.modules.flowable.core.definition.ProcessDefinitionRegistry;

import io.admin.modules.flowable.core.definition.ProcessListener;
import io.admin.modules.flowable.core.dto.ProcessDefinitionInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;


@Slf4j
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Resource
    private GlobalProcessListener globalProcessListener;

    @Resource
    private ProcessConfiguration processConfiguration;

    @Resource
    private ProcessDefinitionRegistry registry;

    @Resource
    @Lazy
    private SysFlowableModelService sysFlowableModelService;

    @Resource
    private SystemHookService systemHookService;



    @Override
    public void configure(SpringProcessEngineConfiguration cfg) {
        // 主键生成器，注意：不会影响act_de开头的表主键生成，因为这是流程设计器的，不是工作流引擎的
        cfg.setIdGenerator(IdTool::uuidV7);
        if (cfg.getEventListeners() == null) {
            cfg.setEventListeners(new ArrayList<>());
        }
        cfg.getEventListeners().add(globalProcessListener);
    }


    @PostConstruct
    void init(){
        for (ProcessDefinition definition : processConfiguration.getDefinitions()) {
            Class<? extends ProcessListener> listener = definition.getListener();
            ProcessListener bean = SpringUtils.getBean(listener);

            String key = definition.getKey();
            String name = definition.getName();

            SysFlowableModel sysFlowableModel = sysFlowableModelService.init(key, name);

            ProcessDefinitionInfo info = new ProcessDefinitionInfo();
            info.setId(sysFlowableModel.getId());
            info.setName(name);
            info.setCode(key);
            info.setFormList(definition.getForms());
            info.setConditionVariableList(definition.getVariables());
            registry.register(key, bean, info);

            log.info("注册流程定义类 {} {}", key, definition.getClass().getName());
            systemHookService.trigger(SystemHookEventType.AFTER_FLOWABLE_DEFINITION_INIT);
        }
    }


}
