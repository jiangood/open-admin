package io.github.jiangood.openadmin.modules.job.entity;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "sys_job_log")
@Getter
@Setter
@FieldNameConstants
public class SysJobLog extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    SysJob sysJob;

    LocalDateTime beginTime;

    LocalDateTime endTime;

    String result;

    // 是否成功
    Boolean success;

    Long jobRunTime;

    @Column(length = 10)
    String executeDate;

    /**
     * 记录聚合桶：null 表示每次一份记录；按小时如 2026-10-19-14；按天如 2026-10-19
     */
    @Column(length = 20)
    String logBucket;

    /**
     * 本周期执行次数
     */
    Integer executeCount;

    /**
     * 本周期失败次数
     */
    Integer failCount;

    /**
     * 本周期累计耗时（毫秒）
     */
    Long totalRunTime;


    @Transient
    public String getJobRunTimeLabel() {
        if (jobRunTime != null) {
            return DateUtil.formatBetween(jobRunTime, BetweenFormatter.Level.SECOND);
        }
        return null;
    }

    @Transient
    public String getTotalRunTimeLabel() {
        if (totalRunTime != null) {
            return DateUtil.formatBetween(totalRunTime, BetweenFormatter.Level.SECOND);
        }
        return null;
    }

    @PrePersist
    public void prePersist() {
        this.executeDate = beginTime == null ? null : beginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (success == null) {
            success = true;
        }
        if (executeCount == null) {
            executeCount = 1;
        }
        if (failCount == null) {
            failCount = 0;
        }
        if (totalRunTime == null) {
            totalRunTime = 0L;
        }
    }
}
