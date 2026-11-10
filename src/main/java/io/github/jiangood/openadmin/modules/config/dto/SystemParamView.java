package io.github.jiangood.openadmin.modules.config.dto;

/**
 * 只读系统参数视图。
 *
 * <p>来自 application.yml / 代码默认值，仅供页面展示，不参与保存。</p>
 */
public record SystemParamView(
        String key,
        String label,
        String value
) {
}
