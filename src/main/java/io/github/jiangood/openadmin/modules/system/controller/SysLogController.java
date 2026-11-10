package io.github.jiangood.openadmin.modules.system.controller;


import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.system.entity.SysBrowserFingerprint;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.service.SysBrowserFingerprintService;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("admin/sys-log")
@RequiredArgsConstructor
public class SysLogController {


    private final SysLogService service;
    private final SysBrowserFingerprintService fingerprintService;


    @HasPermission("sys-log:read")
    @GetMapping("page")
    public AjaxResult page(String dateRange, String operation, String detail, String username, String userId, String ip,
                           String fingerprint,
                           @PageableDefault(sort = "operationTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Spec<SysLog> q = Spec.of();
        q.betweenDateRange(SysLog.Fields.operationTime, dateRange, true);
        q.like(SysLog.Fields.operation, operation);
        q.like(SysLog.Fields.detail, detail);
        q.like(SysLog.Fields.username, username);
        q.eq(SysLog.Fields.userId, userId);
        q.eq(SysLog.Fields.ip, ip);

        // 指纹筛选支持完整指纹或备注（备注名/说明模糊匹配），便于按「张三的电脑」查日志
        if (StringUtils.hasText(fingerprint)) {
            String keyword = fingerprint.trim();
            List<String> remarkFingerprints = fingerprintService.findFingerprintsByRemark(keyword);
            if (remarkFingerprints.isEmpty()) {
                q.eq(SysLog.Fields.fingerprint, keyword);
            } else {
                Spec<SysLog> exact = Spec.<SysLog>of().eq(SysLog.Fields.fingerprint, keyword);
                Spec<SysLog> byRemark = Spec.<SysLog>of().in(SysLog.Fields.fingerprint, remarkFingerprints);
                q.or(exact, byRemark);
            }
        }

        Page<SysLog> page = service.findAll(q, pageable);
        fillFingerprintRemarks(page.getContent());
        return AjaxResult.ok().data(page);
    }

    /**
     * 批量回填当前页日志的指纹备注，避免逐行查库。
     */
    private void fillFingerprintRemarks(List<SysLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        Set<String> fingerprints = logs.stream()
                .map(SysLog::getFingerprint)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, SysBrowserFingerprint> remarkMap = fingerprintService.findMap(fingerprints);
        if (remarkMap.isEmpty()) {
            return;
        }
        for (SysLog item : logs) {
            SysBrowserFingerprint remark = remarkMap.get(item.getFingerprint());
            if (remark != null) {
                item.setFingerprintRemarkId(remark.getId());
                item.setFingerprintName(remark.getName());
                item.setFingerprintRemark(remark.getRemark());
            }
        }
    }


}
