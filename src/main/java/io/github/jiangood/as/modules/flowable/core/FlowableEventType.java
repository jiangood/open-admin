package io.github.jiangood.as.modules.flowable.core;

import lombok.AllArgsConstructor;

// 参考 FlowableEngineEventType， 名称保持一致
@AllArgsConstructor
public enum FlowableEventType {

    TASK_ASSIGNED("任务分配"),
    TASK_COMPLETED("任务完成"),

    /**
     * 流程创建, 创建完成但尚未启动
     * 用途：初始化设置、验证、预处理
     */
    PROCESS_CREATED("流程创建"),

    /**
     * 流程已开始，可以访问所有初始化变量
     * 用途：启动后处理、日志记录、通知、修改业务状态
     */
    PROCESS_STARTED("流程启动"),

    PROCESS_COMPLETED("流程完成"),

    PROCESS_CANCELLED("流程终止");


    final String msg;


}
