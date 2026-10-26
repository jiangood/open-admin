package io.github.jiangood.openadmin.modules.config.dto;

import java.util.List;

/**
 * 配置页分组视图。
 */
public record ConfigGroupView(
        String code,
        String label,
        String description,
        List<ConfigItemView> items
) {
}
