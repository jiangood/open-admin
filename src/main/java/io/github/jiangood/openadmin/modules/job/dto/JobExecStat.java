package io.github.jiangood.openadmin.modules.job.dto;

import io.github.jiangood.openadmin.modules.job.JobStatus;
import lombok.Data;

/**
 * 单个定时任务的执行统计（按状态分组计数）
 */
@Data
public class JobExecStat {

    /**
     * 执行记录总数
     */
    private long recordCount;

    /**
     * 成功次数
     */
    private long successCount;

    /**
     * 部分成功次数
     */
    private long partialCount;

    /**
     * 跳过次数
     */
    private long skipCount;

    /**
     * 业务失败次数
     */
    private long failCount;

    /**
     * 异常次数
     */
    private long exceptionCount;

    /**
     * 已取消次数
     */
    private long cancelledCount;

    /**
     * 运行中数量
     */
    private long runningCount;

    /**
     * 最近一次开始时间
     */
    private String lastBeginTime;

    /**
     * 最近一次结束时间
     */
    private String lastEndTime;

    /**
     * 最近一次状态
     */
    private JobStatus lastStatus;

    /**
     * 最近一次返回结果
     */
    private String lastResult;

    /**
     * 最近一次耗时（毫秒）
     */
    private Long lastRunTime;
}
