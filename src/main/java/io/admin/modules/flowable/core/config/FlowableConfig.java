
package io.admin.modules.flowable.core.config;

import io.admin.common.utils.IdTool;
import io.admin.common.utils.SpringUtils;
import io.admin.common.utils.field.FieldDescription;
import io.admin.framework.config.init.SystemHookEventType;
import io.admin.framework.config.init.SystemHookService;
import io.admin.modules.flowable.admin.dao.SysFlowableModelDao;
import io.admin.modules.flowable.admin.entity.ConditionVariable;
import io.admin.modules.flowable.admin.entity.FormKey;
import io.admin.modules.flowable.admin.entity.SysFlowableModel;
import io.admin.modules.flowable.core.definition.FormKeyDescription;
import io.admin.modules.flowable.core.definition.ProcessDefinition;
import io.admin.modules.flowable.core.definition.ProcessDefinitionDescription;
import io.admin.modules.flowable.core.definition.ProcessDefinitionRegistry;

import io.admin.modules.flowable.dto.ProcessDefinitionInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration>, CommandLineRunner {

    @Resource
    private GlobalProcessListener globalProcessListener;


    @Resource
    @Lazy
    private ProcessDefinitionRegistry registry;

    @Resource
    @Lazy
    private SysFlowableModelDao sysFlowableModelDao;

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

    private void initDefinition() {
        List<ProcessDefinition> definitions = SpringUtils.getBeans(ProcessDefinition.class);
        for (ProcessDefinition definition : definitions) {
            ProcessDefinitionDescription ann = definition.getClass().getAnnotation(ProcessDefinitionDescription.class);
            Assert.notNull(ann, "监听器必须使用注解" + ProcessDefinitionRegistry.class.getSimpleName() + "描述");

            String key = ann.key();



            // 持久化到数据库
            FieldDescription[] fs = ann.conditionVars();
            List<ConditionVariable> vars = new ArrayList<>();
            for (FieldDescription f : fs) {
                ConditionVariable v = new ConditionVariable();
                v.setName(f.name());
                v.setLabel(f.label());
                v.setValueType(f.type());
                vars.add(v);
            }

            FormKeyDescription[] formKeys = ann.formKeys();
            List<FormKey> formKeyList = new ArrayList<>();
            for (FormKeyDescription formKey : formKeys) {
                FormKey fk = new FormKey();
                fk.setValue(formKey.value());
                fk.setLabel(formKey.label());
                formKeyList.add(fk);
            }

            SysFlowableModel sysFlowableModel = sysFlowableModelDao.init(key, ann.name());
            ProcessDefinitionInfo info = new ProcessDefinitionInfo();
            info.setId(sysFlowableModel.getId());
            info.setName(ann.name());
            info.setCode(key);
            info.setFormKeyList(formKeyList);
            info.setConditionVariableList(vars);
            registry.register(key, definition, info);
            log.info("注册流程定义类 {} {}", key, definition.getClass().getName());
        }
        systemHookService.trigger(SystemHookEventType.AFTER_FLOWABLE_DEFINITION_INIT);
    }


    @Override
    public void run(String... args) throws Exception {
        initDefinition();
    }
}
