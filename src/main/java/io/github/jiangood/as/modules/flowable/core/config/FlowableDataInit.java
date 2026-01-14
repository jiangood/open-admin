package io.github.jiangood.as.modules.flowable.core.config;

import io.github.jiangood.as.framework.config.init.SystemHookEventType;
import io.github.jiangood.as.framework.config.init.SystemHookService;
import io.github.jiangood.as.modules.flowable.core.config.meta.ProcessMeta;
import io.github.jiangood.as.modules.flowable.core.service.ProcessMetaService;
import io.github.jiangood.as.modules.flowable.core.service.ProcessService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@AllArgsConstructor
public class FlowableDataInit implements CommandLineRunner {


    private ProcessMetaService processMetaService;

    private SystemHookService systemHookService;
    private ProcessService processService;


    @Override
    public void run(String... args) throws Exception {
        List<ProcessMeta> list = processMetaService.findAll();
        for (ProcessMeta meta : list) {
            String key = meta.getKey();
            processService.initModel(meta);

            log.info("注册流程定义类 {} {}", key, meta.getClass().getName());
            systemHookService.trigger(SystemHookEventType.AFTER_FLOWABLE_DEFINITION_INIT);
        }
    }


}
