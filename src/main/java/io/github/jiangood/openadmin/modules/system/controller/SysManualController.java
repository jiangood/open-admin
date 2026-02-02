package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysManual;
import io.github.jiangood.openadmin.modules.system.service.SysManualService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/sysManual")
public class SysManualController {

    @Resource
    SysManualService service;

    @PreAuthorize("hasAuthority('sysManual:view')")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<SysManual> q = Spec.of();
        q.orLike(searchText, SysManual.Fields.name);

        Page<SysManual> page = service.findAllByUserAction(q, pageable);


        return AjaxResult.ok().data(page);
    }


    @PreAuthorize("hasAuthority('sysManual:save')")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysManual input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByUserAction(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @PreAuthorize("hasAuthority('sysManual:delete')")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByUserAction(id);
        return AjaxResult.ok().msg("删除成功");
    }


}

