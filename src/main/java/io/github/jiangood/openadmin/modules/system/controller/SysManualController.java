package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysManual;
import io.github.jiangood.openadmin.modules.system.service.SysManualService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/sysManual")
@RequiredArgsConstructor
public class SysManualController {

    private final SysManualService service;

    @HasPermission("sys-manual:query")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<SysManual> q = Spec.of();
        q.orLike(searchText, SysManual.Fields.name);

        Page<SysManual> page = service.findAll(q, pageable);


        return AjaxResult.ok().data(page);
    }


    @HasPermission("sys-manual:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysManual input, RequestBodyKeys updateFields) throws Exception {
        service.save(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("sys-manual:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq idRequest) {
        service.deleteById(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }


}

