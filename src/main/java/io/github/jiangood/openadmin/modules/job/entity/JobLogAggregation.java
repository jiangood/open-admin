package io.github.jiangood.openadmin.modules.job.entity;

import io.github.jiangood.openadmin.framework.dict.DictItem;
import io.github.jiangood.openadmin.framework.dict.DictType;

/**
 * 任务执行记录的日志聚合方式
 * <p>
 * 高频任务（如每 5 秒执行）按周期复用同一条 sys_job_log，日志写入同一个文件，
 * 避免执行记录和日志文件数量爆炸。
 */
@DictType(code = "jobLogAggregation", label = "任务日志聚合方式")
public enum JobLogAggregation {

    @DictItem(label = "每次执行", color = "DEFAULT")
    PER_RUN,

    @DictItem(label = "按小时", color = "PROCESSING")
    HOURLY,

    @DictItem(label = "按天", color = "SUCCESS")
    DAILY
}
