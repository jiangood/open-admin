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
     * 正在运行的任务数（内存态进度）
     */
    private long runningCount;

    /**
     * 今日执行次数（按聚合记录的 executeCount 累计）
     */
    private long todayExecuteCount;

    /**
     * 今日失败次数
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
