package io.github.jiangood.as.plugins.flowable.dto.request;

import io.github.jiangood.as.plugins.flowable.dto.TaskHandleType;
import lombok.Data;

@Data
public class HandleTaskRequest {

    TaskHandleType result;
    String taskId;
    String comment;
}
