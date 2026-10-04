package io.github.jiangood.openadmin.modules.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.util.annotation.Remark;
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

@Remark("定时任务")
@Getter
@Setter
@Entity
@Table(name = "sys_job")
@FieldNameConstants
public class SysJob extends BaseEntity { // NOSONAR: 实体以 id 为业务键，继承的 equals 即按 id 比较

    public static final String JOB_SUFFIX = "Job";
    @Column(unique = true)
    @NotNull
    String name;
    String cron;
    @NotNull
    Boolean enabled;
    @NotNull
    String jobClass;
    // 参数
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    Map<String, Object> jobData; // NOSONAR: 仅 Jackson 序列化，不走 Java Serializable
    // 扩展字段
    String extraInfo;


    public SysJob() {

    }


    public SysJob(String id) {
        this.setId(id);
    }

    @JsonIgnore
    @Transient
    public Map<String, Object> getJobDataMap() {
        if (jobData != null) {
            return jobData;
        }

        return Collections.emptyMap();
    }

    @Transient
    public String getJobClassName() {
        return jobClass == null ? null : jobClass.substring(jobClass.lastIndexOf('.') + 1);
    }


    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        Assert.state(jobClass.endsWith(JOB_SUFFIX), "必须以" + JOB_SUFFIX + "结尾");
    }
}
