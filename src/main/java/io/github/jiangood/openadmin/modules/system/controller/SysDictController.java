package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.util.StrUtil;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.config.datadefinition.DictDefinition;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.dto.IdRequest;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.service.SysDictItemService;
import io.github.jiangood.openadmin.modules.system.service.SysDictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictItemService service;
    private final SysDictService sysDictService;

    @PreAuthorize("hasAuthority('sysDict:view')")
    @RequestMapping("tree")
    public AjaxResult tree() throws Exception {

        return AjaxResult.ok().data(sysDictService.tree());
    }


    @PreAuthorize("hasAuthority('sysDict:view')")
    @RequestMapping("page")
    public AjaxResult page(String typeCode) {
        if (StrUtil.isEmpty(typeCode)) {
            return AjaxResult.ok().data(Page.empty());
        }
        List<DictDefinition.Item> items = sysDictService.getItems(typeCode);

        return AjaxResult.ok().data(new PageImpl<>(items));
    }


    @PreAuthorize("hasAuthority('sysDict:save')")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysDictItem param, RequestBodyKeys updateFields) throws Exception {
        SysDictItem result = service.save(param, updateFields);
        return AjaxResult.ok().data(result.getId()).msg("保存成功");
    }


    @PreAuthorize("hasAuthority('sysDict:delete')")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdRequest idRequest) {
        service.delete(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }


}
