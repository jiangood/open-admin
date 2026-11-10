package io.github.jiangood.openadmin.modules.config.dto;

/**
 * 只读系统参数视图。
 *
 * <p>来自 application.yml / 代码默认值，仅供页面展示，不参与保存。</p>
 *
 * @param group 展示分组，见 {@link #GROUP_RUNTIME} / {@link #GROUP_STARTUP}
 */
public record SystemParamView(
        String key,
        String label,
        String value,
        String group
) {

    /** 运行时状态：随服务运行变化，页面会定时刷新 */
    public static final String GROUP_RUNTIME = "runtime";

    /** 启动期配置：来自 application.yml / 代码默认值，改后需重启 */
    public static final String GROUP_STARTUP = "startup";
}
