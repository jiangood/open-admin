package io.github.jiangood.openadmin.modules.config.controller;

import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 系统配置的操作日志入口。
 *
 * <p>配置修改请求体可能包含敏感值，因此不在控制器方法上直接使用 {@code @Log}，
 * 而是由本类只记录变更的 key。参数上的 {@code @RequestBody} 仅用于让
 * {@code LogAspect} 识别并序列化该参数，本类不是控制器，不参与 Web 绑定。</p>
 */
@Component
public class SysConfigOperationLogger {

    @Log("系统配置-修改")
    public AjaxResult updated(@RequestBody List<String> keys) {
        return AjaxResult.ok().data(keys);
    }

    @Log("系统配置-恢复默认")
    public AjaxResult reset(@RequestBody List<String> keys) {
        return AjaxResult.ok().data(keys);
    }
}
