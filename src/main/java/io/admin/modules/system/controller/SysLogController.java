
package io.admin.modules.system.controller;


import io.admin.common.dto.AjaxResult;
import io.admin.framework.config.security.HasPermission;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.SysLog;
import io.admin.modules.system.service.SysLogService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("admin/sysLog")
public class SysLogController {


    @Resource
    private SysLogService service;





    @HasPermission("sysLog:view")
    @RequestMapping("page")
    public AjaxResult page( String dateRange ,String operation, @PageableDefault(sort = "operationTime", direction = Sort.Direction.DESC) Pageable pageable) throws Exception {
        Spec<SysLog> q = Spec.of();
        q.betweenIsoDateRange(SysLog.Fields.operationTime, dateRange,true);
        q.like(SysLog.Fields.operation, operation);

        Page<SysLog> page = service.findPageByRequest(q, pageable);
        return AjaxResult.ok().data(page);
    }


}
