package io.github.jiangood.as.modules.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.as.common.tools.annotation.Remark;
import io.github.jiangood.as.framework.data.DBConstants;
import io.github.jiangood.as.framework.data.converter.ToMapConverter;
import io.github.jiangood.as.framework.data.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

@Remark("作业")
@Getter
@Setter
@Entity
@FieldNameConstants
public class SysJob extends BaseEntity {

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
    @Lob
    @Column(columnDefinition = DBConstants.TYPE_BLOB)
    @Convert(converter = ToMapConverter.class)
    Map<String, Object> jobData;
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


    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        Assert.state(jobClass.endsWith(JOB_SUFFIX), "必须以" + JOB_SUFFIX + "结尾");
    }
}
