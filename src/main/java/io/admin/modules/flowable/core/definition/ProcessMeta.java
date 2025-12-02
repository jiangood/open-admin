package io.admin.modules.flowable.core.definition;

import lombok.Data;

import java.util.List;

@Data
public class ProcessMeta {
    private String key;
    private String name;

    private Class<? extends ProcessListener> listener;

    private List<ProcessVariable> variables;
    private List<FormDefinition> forms;
}
