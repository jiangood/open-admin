package io.github.jiangood.openadmin.modules.job.entity;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.modules.job.JobStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 定时任务执行记录。
 *
 * <p>每次执行一行：开始时写入 {@link JobStatus#RUNNING}，结束时更新为终态。
 * 状态是成功/失败判定的唯一依据。</p>
 */
@Entity
@Table(name = "sys_job_log", indexes = {
        @Index(name = "idx_sys_job_log_status", columnList = "status")
})
@Getter
@Setter
@FieldNameConstants
public class SysJobLog extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    SysJob sysJob;

    LocalDateTime beginTime;

    LocalDateTime endTime;

    /**
     * 返回结果（人类可读的一句话说明）
     */
    String result;

    /**
     * 执行状态
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    JobStatus status;

    /**
     * Quartz 触发实例 ID：本次执行的唯一标识，用于判断记录对应的一次执行是否仍在调度器中
     */
    @Column(length = 64)
    String fireInstanceId;

    Long jobRunTime;

    @Transient
    public String getJobRunTimeLabel() {
        if (jobRunTime != null) {
            return DateUtil.formatBetween(jobRunTime, BetweenFormatter.Level.SECOND);
        }
        return null;
    }
}
