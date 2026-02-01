package io.github.jiangood.openadmin.modules.job;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.jiangood.openadmin.lang.field.Field;

import java.util.List;
import java.util.Map;

public interface JobParamFieldProvider {

    List<Field> getFields(JobDescription jobDesc, Map<String, Object> jobData) throws JsonProcessingException;

}
