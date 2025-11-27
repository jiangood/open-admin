package io.admin.modules.flowable.demo;

import io.admin.common.utils.field.FieldDescription;
import io.admin.common.utils.field.ValueType;
import io.admin.modules.flowable.core.FlowableEventType;
import io.admin.modules.flowable.core.definition.FormKeyDescription;
import io.admin.modules.flowable.core.definition.ProcessDefinition;
import io.admin.modules.flowable.core.definition.ProcessDefinitionDescription;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ProcessDefinitionDescription(key = "demo",name = "demo-请假流程",
        conditionVars = {@FieldDescription(name = "days",label = "请假天数",type = ValueType.NUMBER)},
        formKeys = {@FormKeyDescription(value = "driverForm",label = "司机表单"),
                    @FormKeyDescription(value = "finishForm",label = "结束表单")
})
public class DemoProcess implements ProcessDefinition {

    @Override
    public void onProcessEvent(FlowableEventType type, String initiator, String businessKey, Map<String, Object> variables) {

    }
}
