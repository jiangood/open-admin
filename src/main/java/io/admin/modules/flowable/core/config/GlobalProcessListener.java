package io.admin.modules.flowable.core.config;

import io.admin.modules.flowable.core.FlowableEventType;
import io.admin.modules.flowable.core.definition.ProcessDefinition;
import io.admin.modules.flowable.core.definition.ProcessDefinitionRegistry;
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
    ProcessDefinitionRegistry registry;


    @Override
    public void onEvent(FlowableEvent flowableEvent) {
        if (!(flowableEvent instanceof FlowableProcessEventImpl event)) {
            return;
        }

        log.trace("流程事件 {} ",flowableEvent);

        String typeName = flowableEvent.getType().name();
        long count = Arrays.stream(FlowableEventType.values()).filter(t -> t.name().equals(typeName)).count();
        if(count ==0){
            return;
        }

        FlowableEventType eventType = FlowableEventType.valueOf(typeName);

        String instanceId = event.getProcessInstanceId();
        ExecutionEntityImpl execution = (ExecutionEntityImpl) event.getExecution();
        String definitionKey = execution.getProcessDefinitionKey();


        ProcessDefinition listener = registry.getDefinition(definitionKey);
        if (listener == null) {
            return;
        }


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
        listener.onProcessEvent(eventType, initiator, businessKey, variables);
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
