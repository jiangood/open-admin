package io.github.jiangood.as.modules.api.controller;

import io.github.jiangood.as.common.dto.AjaxResult;
import io.github.jiangood.as.modules.api.entity.ApiAccessLog;
import io.github.jiangood.as.modules.api.service.ApiAccessLogService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/apiAccessLog")
public class ApiAccessLogController {

    @Resource
    ApiAccessLogService service;


    @Deprecated
    @RequestMapping("page")
    public AjaxResult page(@PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Page<ApiAccessLog> page = service.findPageByRequest(null, pageable);
        return AjaxResult.ok().data(page);
    }


}

