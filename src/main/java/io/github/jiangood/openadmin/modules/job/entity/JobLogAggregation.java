package io.github.jiangood.openadmin.modules.job.entity;

import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;

/**
 * 任务执行记录方式（每次/每小时/每天一份记录）
 * <p>
 * 高频任务（如每 5 秒执行）按周期复用同一条 sys_job_log，日志写入同一个文件，
 * 避免执行记录和日志文件数量爆炸。
 */
@DictType(code = "jobLogAggregation", label = "任务执行记录方式")
public enum JobLogAggregation {

    @DictItem(label = "每次一份记录", color = "DEFAULT")
    PER_RUN,

    @DictItem(label = "每小时一份记录", color = "PROCESSING")
    HOURLY,

    @DictItem(label = "每天一份记录", color = "SUCCESS")
    DAILY
}
