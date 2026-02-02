package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.service.SysDictItemService;
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
@RequestMapping("admin/sysDictItem")
public class SysDictItemController {

    @Resource
    private SysDictItemService service;

    @PreAuthorize("hasAuthority('sysDict:view')")
    @RequestMapping("page")
    public AjaxResult page(String sysDictId, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        if (StrUtil.isNotEmpty(sysDictId)) {
            Spec<SysDictItem> q = service.spec();
            q.eq(SysDictItem.Fields.sysDict + ".id", sysDictId);
            Page<SysDictItem> page = service.findAllByUserAction(q, pageable);
            return AjaxResult.ok().data(page);
        } else {
            return AjaxResult.ok().data(Page.empty(pageable));
        }
    }


    @PreAuthorize("hasAuthority('sysDict:save')")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysDictItem param, RequestBodyKeys updateFields) throws Exception {
        SysDictItem result = service.saveOrUpdateByUserAction(param, updateFields);
        return AjaxResult.ok().data(result.getId()).msg("保存成功");
    }


    @PreAuthorize("hasAuthority('sysDict:delete')")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByUserAction(id);
        return AjaxResult.ok().msg("删除成功");
    }


}
