package io.github.jiangood.sa.modules.flowable.core.config;

import io.github.jiangood.sa.framework.config.init.SystemHookEventType;
import io.github.jiangood.sa.framework.config.init.SystemHookService;
import io.github.jiangood.sa.modules.flowable.core.config.meta.ProcessMeta;
import io.github.jiangood.sa.modules.flowable.core.service.ProcessService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@AllArgsConstructor
public class FlowableDataInit implements CommandLineRunner {


    private ProcessMetaCfg processConfiguration;

    private SystemHookService systemHookService;
    private ProcessService processService;


    @Override
    public void run(String... args) throws Exception {
        for (ProcessMeta meta : processConfiguration.getList()) {

            String key = meta.getKey();

            processService.createProcessDefinition(meta);


            log.info("注册流程定义类 {} {}", key, meta.getClass().getName());
            systemHookService.trigger(SystemHookEventType.AFTER_FLOWABLE_DEFINITION_INIT);
        }
    }




}
