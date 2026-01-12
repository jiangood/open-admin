package io.github.jiangood.as.modules.flowable.core.config;

import io.github.jiangood.as.common.tools.SpringTool;
import io.github.jiangood.as.modules.flowable.core.FlowableEventType;
import io.github.jiangood.as.modules.flowable.core.config.meta.ProcessListener;
import io.github.jiangood.as.modules.flowable.core.config.meta.ProcessMeta;
import io.github.jiangood.as.modules.flowable.core.service.ProcessMetaService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.HistoryService;
import org.flowable.engine.delegate.event.impl.FlowableProcessEventImpl;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class GlobalProcessListener implements FlowableEventListener {

    @Lazy
    @Resource
    HistoryService historyService;

    @Resource
    ProcessMetaService processMetaService;


    @Override
    public void onEvent(FlowableEvent flowableEvent) {
        if (!(flowableEvent instanceof FlowableProcessEventImpl event)) {
            return;
        }

        log.trace("流程事件 {} ", flowableEvent);

        String typeName = flowableEvent.getType().name();
        long count = Arrays.stream(FlowableEventType.values()).filter(t -> t.name().equals(typeName)).count();
        if (count == 0) {
            return;
        }

        FlowableEventType eventType = FlowableEventType.valueOf(typeName);

        String instanceId = event.getProcessInstanceId();
        ExecutionEntityImpl execution = (ExecutionEntityImpl) event.getExecution();
        String definitionKey = execution.getProcessDefinitionKey();


        log.info("流程事件 {} {}", definitionKey, event.getType());

        Map<String, Object> variables = execution.getVariables();
        String businessKey = (String) variables.get("BUSINESS_KEY");
        String initiator = (String) variables.get("INITIATOR");

        // 兼容性代码
        if (businessKey == null) {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).singleResult();
            if (historicProcessInstance == null) {
                return;
            }
            businessKey = historicProcessInstance.getBusinessKey();
        }


        // 触发
        ProcessMeta meta = processMetaService.findOne(definitionKey);
        if (meta != null) {
            Class<? extends ProcessListener> listener = meta.getListener();
            if (listener != null) {
                ProcessListener bean = SpringTool.getBean(listener);
                if (bean != null) {
                    bean.onProcessEvent(eventType, initiator, businessKey, variables);
                }
            }
        }


    }


    @Override
    public boolean isFailOnException() {
        return true;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }


}
