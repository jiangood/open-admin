package io.github.jiangood.openadmin.modules.config.dto;

import java.util.List;

/**
 * 修改配置请求。只提交改动的项。
 */
public record ConfigUpdateReq(List<Item> values) {

    public record Item(String key, Object value) {
    }
}
