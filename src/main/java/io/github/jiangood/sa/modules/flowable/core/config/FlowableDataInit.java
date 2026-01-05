package io.github.jiangood.sa.modules.flowable.core.config;

import io.github.jiangood.sa.framework.config.init.SystemHookEventType;
import io.github.jiangood.sa.framework.config.init.SystemHookService;
import io.github.jiangood.sa.modules.flowable.core.config.meta.ProcessMeta;
import io.github.jiangood.sa.modules.flowable.core.service.FlowableService;
import io.github.jiangood.sa.modules.flowable.utils.ModelTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;


@Slf4j
@Component
@AllArgsConstructor
public class FlowableDataInit implements CommandLineRunner {


    private ProcessMetaCfg processConfiguration;

    private SystemHookService systemHookService;
    private FlowableService flowableService;


    @Override
    public void run(String... args) throws Exception {
        for (ProcessMeta meta : processConfiguration.getList()) {

            String key = meta.getKey();

            flowableService.createProcessDefinition(meta);


            log.info("注册流程定义类 {} {}", key, meta.getClass().getName());
            systemHookService.trigger(SystemHookEventType.AFTER_FLOWABLE_DEFINITION_INIT);
        }
    }




}
