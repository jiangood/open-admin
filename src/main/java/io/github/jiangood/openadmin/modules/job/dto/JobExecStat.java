package io.github.jiangood.openadmin.modules.job.dto;

import lombok.Data;

/**
 * 单个定时任务的执行统计
 */
@Data
public class JobExecStat {

    /**
     * 执行记录条数
     */
    private long recordCount;

    /**
     * 累计执行次数（聚合记录的 executeCount 之和）
     */
    private long totalExecuteCount;

    /**
     * 累计失败次数
     */
    private long totalFailCount;

    /**
     * 最近一次开始时间
     */
    private String lastBeginTime;

    /**
     * 最近一次结束时间
     */
    private String lastEndTime;

    /**
     * 最近一次是否成功
     */
    private Boolean lastSuccess;

    /**
     * 最近一次返回结果
     */
    private String lastResult;

    /**
     * 最近一次耗时（毫秒）
     */
    private Long lastRunTime;
}