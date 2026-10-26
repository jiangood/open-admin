package io.github.jiangood.openadmin.modules.config.dto;

import io.github.jiangood.openadmin.util.dto.Option;

import java.util.List;

/**
 * 配置项视图。
 *
 * <p>敏感项不回显 {@code value} / {@code defaultValue}，只通过 {@code hasValue} 表示是否已设置。</p>
 */
public record ConfigItemView(
        String key,
        String label,
        String hint,
        String type,
        Object defaultValue,
        Object value,
        boolean overridden,
        boolean secret,
        boolean hasValue,
        Double min,
        Double max,
        List<Option> options,
        String source,
        boolean required
) {
}
