package io.github.jiangood.sa.modules.flowable.core.config.meta;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProcessMeta {
    private String key;
    private String name;

    private Class<? extends ProcessListener> listener;

    private List<ProcessVariable> variables = new ArrayList<>();
    private List<FormDefinition> forms = new ArrayList<>();
}
