package io.github.jiangood.as.plugins.flowable.example;

import io.github.jiangood.as.plugins.flowable.core.FlowableEventType;
import io.github.jiangood.as.plugins.flowable.config.meta.ProcessListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LeaveProcessListener implements ProcessListener {

    @Override
    public void onProcessEvent(FlowableEventType type, String initiator, String businessKey, Map<String, Object> variables) {

    }
}
