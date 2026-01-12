package io.github.jiangood.as.modules.job;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.jiangood.as.common.tools.field.Field;

import java.util.List;
import java.util.Map;

public interface JobParamFieldProvider {

    List<Field> getFields(JobDescription jobDesc, Map<String, Object> jobData) throws JsonProcessingException;

}
