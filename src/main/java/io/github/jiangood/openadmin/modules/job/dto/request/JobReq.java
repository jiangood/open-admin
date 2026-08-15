package io.github.jiangood.openadmin.modules.job.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class JobReq {
    String id;
    String name;
    String cron;
    Boolean enabled;
    String jobClass;
    Map<String, Object> jobData;
    String extraInfo;
}
