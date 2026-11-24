package io.github.jiangood.openadmin.modules.job;

/**
 * 定时任务执行结果：状态 + 一句话说明。
 *
 * <p>任务实现只应使用 {@link #ok()}、{@link #partial(String)}、{@link #skip(String)}、
 * {@link #fail(String)} 四个入口；{@link JobStatus#RUNNING}、{@link JobStatus#EXCEPTION}、
 * {@link JobStatus#CANCELLED} 由框架产生。</p>
 *
 * <p>{@code message} 会展示在管理页「返回结果」列，长度超过 240 字符时由框架截断，
 * 完整信息请写入任务日志。</p>
 */
public record JobResult(JobStatus status, String message) {

    public static JobResult ok() {
        return new JobResult(JobStatus.SUCCESS, null);
    }

    public static JobResult ok(String message) {
        return new JobResult(JobStatus.SUCCESS, message);
    }

    public static JobResult partial(String message) {
        return new JobResult(JobStatus.PARTIAL, message);
    }

    public static JobResult skip(String message) {
        return new JobResult(JobStatus.SKIPPED, message);
    }

    public static JobResult fail(String message) {
        return new JobResult(JobStatus.FAILED, message);
    }
}
