package io.admin.modules.flowable.core.definition;


import com.google.common.collect.Lists;
import io.admin.modules.flowable.dto.ProcessDefinitionInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessDefinitionRegistry {

    private final Map<String, ProcessDefinition> definitionMap = new HashMap<>();

    private final Map<String, ProcessDefinitionInfo> dtoMap = new HashMap<>();



    public void register(String key, ProcessDefinition definition, ProcessDefinitionInfo dto) {
        Assert.state(!definitionMap.containsKey(key), "流程监听器只能设置一个");
        definitionMap.put(key, definition);
        dtoMap.put(key, dto);
    }

    public ProcessDefinition getDefinition(String key) {
        return definitionMap.get(key);
    }

    public ProcessDefinitionInfo getInfo(String key) {
    	return dtoMap.get(key);
    }

    public List<ProcessDefinitionInfo> getAll(){
        return Lists.newArrayList(dtoMap.values());
    }

}
