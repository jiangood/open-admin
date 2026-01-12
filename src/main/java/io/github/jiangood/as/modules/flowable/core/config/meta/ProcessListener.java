package io.github.jiangood.as.modules.flowable.core.config.meta;

import io.github.jiangood.as.modules.flowable.core.FlowableEventType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 流程定义接口
 */
public interface ProcessListener {


    /**
     * @param type
     * @param initiator   发起人
     * @param businessKey 业务标识，如key
     * @param variables   变量
     */
    @Transactional
    void onProcessEvent(FlowableEventType type, String initiator, String businessKey, Map<String, Object> variables);


}
