package io.github.jiangood.openadmin.modules.config.dto;

import java.util.List;

/**
 * 恢复默认请求。{@code keys} 为空表示恢复全部配置。
 */
public record ConfigResetReq(List<String> keys) {
}
