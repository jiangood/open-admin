package io.github.jiangood.openadmin.modules.job.dto;

import lombok.Data;

/**
 * 任务日志文件占用统计（只读）
 */
@Data
public class LogSizeStat {

    /**
     * 字节数
     */
    private long bytes;

    /**
     * 文件个数
     */
    private long files;

    /**
     * 告警阈值（MB），未开启告警时为 null；仅全量统计返回
     */
    private Long warnMb;

    /**
     * 是否已超过告警阈值，仅全量统计返回
     */
    private Boolean warn;
}
