package io.github.jiangood.openadmin.modules.job.progress;

/**
 * 任务运行状态（内存态，仅用于前端展示运行中的任务）
 */
public enum JobRunState {

    RUNNING("运行中", "processing"),
    SUCCESS("成功", "success"),
    FAILED("失败", "error");

    private final String label;
    private final String color;

    JobRunState(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
