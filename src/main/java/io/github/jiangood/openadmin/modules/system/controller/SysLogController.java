package io.github.jiangood.openadmin.modules.system.controller;


import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("admin/sysLog")
@RequiredArgsConstructor
public class SysLogController {


    private final SysLogService service;


    @HasPermission("sys-log:read")
    @GetMapping("page")
    public AjaxResult page(String dateRange, String operation, @PageableDefault(sort = "operationTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Spec<SysLog> q = Spec.of();
        q.betweenDateRange(SysLog.Fields.operationTime, dateRange, true);
        q.like(SysLog.Fields.operation, operation);

        Page<SysLog> page = service.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }


}
