package io.admin.modules.flowable.core;

import java.util.Map;

public interface FlowableManager {
    void start(String processDefinitionKey, String bizKey, Map<String, Object> variables);

    void start(String processDefinitionKey, String bizKey, String title, Map<String, Object> variables);



}
