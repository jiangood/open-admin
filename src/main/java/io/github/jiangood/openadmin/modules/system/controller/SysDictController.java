package io.github.jiangood.openadmin.modules.system.controller;


import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysDict;
import io.github.jiangood.openadmin.modules.system.service.SysDictService;
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
@RequestMapping("admin/sysDict")
public class SysDictController {

    @Resource
    private SysDictService service;

    @PreAuthorize("hasAuthority('sysDict:view')")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<SysDict> q = service.spec();
        q.orLike(searchText, "text", "code");

        Page<SysDict> page = service.findAllByUserAction(q, pageable);

        return AjaxResult.ok().data(page);
    }

    @PreAuthorize("hasAuthority('sysDict:save')")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysDict input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByUserAction(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @PreAuthorize("hasAuthority('sysDict:delete')")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByUserAction(id);
        return AjaxResult.ok().msg("删除成功");
    }


}
