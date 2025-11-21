
package io.admin.modules.system.controller;

import cn.hutool.core.util.StrUtil;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.data.query.JpaQuery;
import io.admin.modules.system.entity.SysDict;
import io.admin.modules.system.entity.SysDictItem;
import io.admin.modules.system.service.SysDictItemService;
import io.admin.modules.system.service.SysDictService;
import io.admin.framework.config.security.HasPermission;
import io.admin.common.dto.AjaxResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("admin/sysDictItem")
public class SysDictItemController  {

    @Resource
    private SysDictItemService service;

    @Resource
    private SysDictService sysDictService;




    @HasPermission("sysDict:view")
    @RequestMapping("page")
    public AjaxResult page( String sysDictId, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        JpaQuery<SysDictItem> q = new JpaQuery<>();
        if(StrUtil.isNotEmpty(sysDictId)){
            q.eq(SysDictItem.Fields.sysDict + ".id",  sysDictId);
            Page<SysDictItem> page = service.pageByRequest(q, pageable);
            return AjaxResult.ok().data(page);
        }else {
            return AjaxResult.ok().data(Page.empty(pageable));
        }
    }


    @HasPermission("sysDict:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysDictItem param, RequestBodyKeys updateFields) throws Exception {
        SysDict dict = sysDictService.findByRequest(param.getSysDict().getId());

        param.setBuiltin(false);
        SysDictItem result = service.saveOrUpdateByRequest(param,updateFields);
        return AjaxResult.ok().data( result.getId()).msg("保存成功");
    }



    @HasPermission("sysDict:delete")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }


}
