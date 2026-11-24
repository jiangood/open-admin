package io.github.jiangood.openadmin.modules.job;

import java.util.List;

/**
 * 定时任务执行状态。
 *
 * <p>{@link #RUNNING} 由框架在任务开始时写入，其余状态在任务结束时写入；
 * {@link #EXCEPTION}、{@link #CANCELLED} 由框架根据执行过程产生，任务实现不应主动返回。</p>
 */
public enum JobStatus {

    /** 运行中：已开始执行、尚未结束 */
    RUNNING("运行中"),

    /** 成功：任务正常完成 */
    SUCCESS("成功"),

    /** 部分成功：主体工作完成，但存在子项失败 */
    PARTIAL("部分成功"),

    /** 跳过：前置条件不满足，主动不执行（不算成功也不算失败） */
    SKIPPED("跳过"),

    /** 失败：任务正常返回，但业务结论为失败 */
    FAILED("失败"),

    /** 异常：执行过程中抛出异常 / Error，未能正常返回 */
    EXCEPTION("异常"),

    /** 已取消：管理端中断了执行线程 */
    CANCELLED("已取消");

    /**
     * 计入「失败」口径的状态，用于统计
     */
    public static final List<JobStatus> FAILURE_STATUSES = List.of(PARTIAL, FAILED, EXCEPTION, CANCELLED);

    private final String label;

    JobStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
