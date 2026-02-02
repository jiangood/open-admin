package io.github.jiangood.openadmin.modules.system.controller;


import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.service.SysLogService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("admin/sysLog")
public class SysLogController {


    @Resource
    private SysLogService service;


    @PreAuthorize("hasAuthority('sysLog:view')")
    @RequestMapping("page")
    public AjaxResult page(String dateRange, String operation, @PageableDefault(sort = "operationTime", direction = Sort.Direction.DESC) Pageable pageable) throws Exception {
        Spec<SysLog> q = Spec.of();
        q.betweenDateRange(SysLog.Fields.operationTime, dateRange, true);
        q.like(SysLog.Fields.operation, operation);

        Page<SysLog> page = service.findAllByUserAction(q, pageable);
        return AjaxResult.ok().data(page);
    }


}
