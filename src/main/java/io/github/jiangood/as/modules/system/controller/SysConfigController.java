package io.github.jiangood.as.modules.system.controller;


import io.github.jiangood.as.common.dto.AjaxResult;
import io.github.jiangood.as.framework.log.Log;
import io.github.jiangood.as.modules.system.dto.response.SysConfigResponse;
import io.github.jiangood.as.modules.system.entity.SysConfig;
import io.github.jiangood.as.modules.system.service.SysConfigService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 参数配置
 */
@RestController
@RequestMapping("admin/sysConfig")
public class SysConfigController {

    @Resource
    private SysConfigService service;


    @PreAuthorize("hasAuthority('sysConfig:view')")
    @RequestMapping("page")
    public AjaxResult page(String searchText) throws Exception {
        List<SysConfigResponse> list = service.findAllByRequest(searchText);

        return AjaxResult.ok().data(list);
    }

    @Log("修改系统配置")
    @PreAuthorize("hasAuthority('sysConfig:save')")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysConfig param) throws Exception {
        service.save(param);
        return AjaxResult.ok().msg("保存成功");
    }


}


