package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.log.OperationLogContext;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.framework.web.CrudOps;
import io.github.jiangood.openadmin.modules.system.dto.request.FingerprintRemarkReq;
import io.github.jiangood.openadmin.modules.system.entity.SysBrowserFingerprint;
import io.github.jiangood.openadmin.modules.system.service.SysBrowserFingerprintService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 浏览器指纹备注：在操作日志页给指纹加备注名，便于识别是哪个人/哪台设备。
 */
@RestController
@RequestMapping("admin/sys-browser-fingerprint")
@RequiredArgsConstructor
public class SysBrowserFingerprintController {

    private final SysBrowserFingerprintService service;

    @HasPermission("sys-log:write")
    @Log("操作日志-标记浏览器指纹")
    @PostMapping("save")
    public AjaxResult save(@Valid @RequestBody FingerprintRemarkReq req) {
        SysBrowserFingerprint entity = service.saveRemark(req.getFingerprint(), req.getName(), req.getRemark());

        if (entity == null) {
            OperationLogContext.setDetail("取消指纹标记「" + req.getFingerprint() + "」");
            return AjaxResult.ok("已取消标记");
        }

        String label = entity.getName() != null ? entity.getName() : entity.getRemark();
        OperationLogContext.setDetail("标记浏览器指纹「" + req.getFingerprint() + "」"
                + (label != null ? "为「" + label + "」" : ""));
        return AjaxResult.ok("保存成功").data(entity);
    }

    @HasPermission("sys-log:write")
    @Log("操作日志-取消指纹标记")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq req) {
        SysBrowserFingerprint entity = CrudOps.requireDelete(service.findById(req.getId()), "指纹标记不存在");
        OperationLogContext.setDetail("取消指纹标记「" + label(entity) + "」");
        service.deleteById(req.getId());
        return AjaxResult.ok("已取消标记");
    }

    private static String label(SysBrowserFingerprint entity) {
        if (entity.getName() != null) {
            return entity.getName();
        }
        if (entity.getRemark() != null) {
            return entity.getRemark();
        }
        return entity.getFingerprint();
    }
}
