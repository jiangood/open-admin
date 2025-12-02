
package io.admin.modules.flowable.core.config;

import io.admin.framework.config.init.SystemHookEventType;
import io.admin.framework.config.init.SystemHookService;
import io.admin.modules.flowable.core.definition.ProcessMeta;
import io.admin.modules.flowable.utils.ModelUtils;
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
    private RepositoryService repositoryService;


    @Override
    public void run(String... args) throws Exception {
        for (ProcessMeta definition : processConfiguration.getList()) {

            String key = definition.getKey();
            String name = definition.getName();

            this.init(key, name);


            log.info("注册流程定义类 {} {}", key, definition.getClass().getName());
            systemHookService.trigger(SystemHookEventType.AFTER_FLOWABLE_DEFINITION_INIT);
        }
    }

    public void init(String key, String name) {
        log.info("初始化流程定义 {} {}  ", key, name);

        long count = repositoryService.createModelQuery().modelKey(key).count();
        if (count > 0) {
            return;
        }

        Model m = repositoryService.newModel();
        m.setName(name);
        m.setKey(key);
        repositoryService.saveModel(m);


        // create default model xml
        BpmnModel model = new BpmnModel();
        Process proc = new Process();
        proc.setExecutable(true);
        proc.setId(key);
        proc.setName(name);
        model.addProcess(proc);

        StartEvent startEvent = new StartEvent();
        startEvent.setId("StartEvent_1");
        proc.addFlowElement(startEvent);

        model.addGraphicInfo(startEvent.getId(), new GraphicInfo(200, 200, 30, 30));

        String xml = ModelUtils.modelToXml(model);

        repositoryService.addModelEditorSource(m.getId(), xml.getBytes(StandardCharsets.UTF_8));
    }


}
