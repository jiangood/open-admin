package io.github.jiangood.openadmin.modules.config.controller;

import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.config.dto.ConfigResetReq;
import io.github.jiangood.openadmin.modules.config.dto.ConfigUpdateReq;
import io.github.jiangood.openadmin.modules.config.service.SysConfigService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置接口。
 *
 * <p>新增/修改配置项请在业务模块实现 {@code ConfigProvider}，不要直接改本控制器。</p>
 */
@RestController
@RequestMapping("admin/sys-config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;
    private final SysConfigOperationLogger operationLogger;

    @HasPermission("sys-config:read")
    @GetMapping("items")
    public AjaxResult items() {
        return AjaxResult.ok().data(Map.of("groups", configService.groups()));
    }

    /**
     * 只提交改动项。注意：本方法不要加 {@code @Log}，请求体可能包含敏感值；
     * 操作日志由 {@link SysConfigOperationLogger} 只记录变更的 key。
     */
    @HasPermission("sys-config:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody ConfigUpdateReq req) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (req != null && req.values() != null) {
            for (ConfigUpdateReq.Item item : req.values()) {
                if (item != null && item.key() != null) {
                    values.put(item.key(), item.value());
                }
            }
        }

        List<String> changed = configService.update(values);
        if (!changed.isEmpty()) {
            operationLogger.updated(changed);
        }
        return AjaxResult.ok().msg("已保存").data(Map.of("groups", configService.groups()));
    }

    @HasPermission("sys-config:update")
    @PostMapping("reset")
    public AjaxResult reset(@RequestBody(required = false) ConfigResetReq req) {
        List<String> keys = req == null ? null : req.keys();
        configService.reset(keys);
        operationLogger.reset(keys == null ? List.of() : keys);
        return AjaxResult.ok().msg("已恢复默认").data(Map.of("groups", configService.groups()));
    }

    @HasPermission("sys-config:read")
    @GetMapping("value")
    public AjaxResult value(@RequestParam String key) {
        return AjaxResult.ok().data(configService.item(key));
    }
}
