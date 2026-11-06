package io.github.jiangood.openadmin.modules.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jiangood.openadmin.modules.job.entity.JobLogAggregation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.quartz.Job;

import java.util.Map;

/**
 * 定时任务声明。
 *
 * <p>扫描 {@link JobDescription} 和业务通过 {@link JobRegistrar} 显式声明的任务，
 * 最终都会转换成该对象，再统一补建到 {@code sys_job}。</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDefinition {

    /**
     * 稳定标识，默认全限定类名
     */
    private String key;

    @JsonIgnore
    private Class<? extends Job> jobClass;

    private String name;

    private String cron;

    private Boolean enabled;

    private JobLogAggregation logAggregation;

    private Map<String, Object> jobData;

    private String extraInfo;

    /**
     * 是否参与启动自动注册；SPI 显式声明的任务恒为 true。
     * 不对外序列化，仅框架内部使用。
     */
    @JsonIgnore
    @Builder.Default
    private boolean autoRegister = true;

    public String getJobClassName() {
        return jobClass == null ? null : jobClass.getName();
    }

}
