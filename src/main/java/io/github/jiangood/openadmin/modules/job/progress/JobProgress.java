package io.github.jiangood.openadmin.modules.job.progress;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 任务运行进度快照（内存态，不落库）
 * <p>
 * 每次更新都是整体替换一个新实例，保证 Web 线程读到的是一致快照。
 */
@Getter
@Setter
public class JobProgress {

    /**
     * 任务定义 ID（SysJob.id）
     */
    private String jobId;
    private String jobName;
    /**
     * 本次执行记录 ID（SysJobLog.id），前端可据此查看日志
     */
    private String logId;
    private JobRunState state;
    /**
     * 百分比 0-100，未知时为 null
     */
    private Integer percent;
    private Long current;
    private Long total;
    /**
     * 进度描述，如「已同步 3/10」
     */
    private String message;
    private LocalDateTime beginTime;
    private LocalDateTime updateTime;
    private LocalDateTime endTime;
    /**
     * 已耗时（毫秒）
     */
    private Long runTime;
    /**
     * 执行结果（结束后写入）
     */
    private String result;

    public String getStateLabel() {
        return state == null ? null : state.getLabel();
    }

    public String getStateColor() {
        return state == null ? null : state.getColor();
    }

    public boolean isRunning() {
        return state == JobRunState.RUNNING;
    }
}
