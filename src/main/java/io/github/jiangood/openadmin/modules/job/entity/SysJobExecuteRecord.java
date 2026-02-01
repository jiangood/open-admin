package io.github.jiangood.openadmin.modules.job.entity;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

@Entity
@Getter
@Setter
@FieldNameConstants
public class SysJobExecuteRecord extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    SysJob sysJob;

    Date beginTime;

    Date endTime;

    String result;

    // 是否成功
    Boolean success;

    Long jobRunTime;

    @Column(length = 10)
    String executeDate;


    @Transient
    public String getJobRunTimeLabel() {
        if (jobRunTime != null) {
            String str = DateUtil.formatBetween(jobRunTime, BetweenFormatter.Level.SECOND);
            return str;
        }
        return null;
    }

    @PrePersist
    public void prePersist() {
        this.executeDate = DateUtil.formatDate(beginTime);
        if (success == null) {
            success = true;
        }
    }
}
