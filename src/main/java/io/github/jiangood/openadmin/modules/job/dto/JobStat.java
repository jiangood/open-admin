package io.github.jiangood.openadmin.modules.job.dto;

import lombok.Data;

/**
 * 定时任务页面顶部统计信息
 */
@Data
public class JobStat {

    /**
     * 任务总数
     */
    private long jobCount;

    /**
     * 启用中的任务数
     */
    private long enabledCount;

    /**
     * 今日执行记录数（含运行中与跳过）
     */
    private long todayExecuteCount;

    /**
     * 今日失败次数（部分成功 / 失败 / 异常 / 取消）
     */
    private long todayFailCount;

    /**
     * Quartz 调度器信息
     */
    private Scheduler scheduler;

    @Data
    public static class Scheduler {

        private String version;

        private String schedulerName;

        private String instanceId;

        /**
         * 调度器是否已启动
         */
        private boolean started;

        /**
         * 是否处于待机模式
         */
        private boolean standby;

        /**
         * 启动时间
         */
        private String runningSince;

        /**
         * 已执行任务数
         */
        private long jobsExecuted;

        /**
         * 线程池大小
         */
        private int threadPoolSize;

        /**
         * 作业存储实现类简名
         */
        private String jobStoreClass;

        /**
         * 作业存储是否支持持久化
         */
        private boolean persistent;

        /**
         * 作业存储是否集群化
         */
        private boolean clustered;

        /**
         * 读取调度器信息失败时的错误描述
         */
        private String error;
    }
}
