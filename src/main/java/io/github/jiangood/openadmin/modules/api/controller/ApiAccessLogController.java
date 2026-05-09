package io.github.jiangood.openadmin.modules.api.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.modules.api.entity.ApiLog;
import io.github.jiangood.openadmin.modules.api.service.ApiAccessLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/apiLog")
@RequiredArgsConstructor
public class ApiAccessLogController {

    private final ApiAccessLogService service;


    @Deprecated
    @RequestMapping("page")
    public AjaxResult page(@PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Page<ApiLog> page = service.findAll(null, pageable);
        return AjaxResult.ok().data(page);
    }


}

