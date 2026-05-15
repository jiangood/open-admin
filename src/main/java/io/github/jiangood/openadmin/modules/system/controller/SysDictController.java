package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.Option;
import io.github.jiangood.openadmin.modules.system.dto.DictItemVO;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.service.SysDictItemService;
import io.github.jiangood.openadmin.modules.system.service.SysDictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("admin/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictItemService itemService;
    private final SysDictService sysDictService;




    @HasPermission("sys-dict:read")
    @RequestMapping("page")
    public AjaxResult page(String searchText) {
        List<DictItemVO> list = sysDictService.getAllItems();
        if(searchText != null){
            list = list.stream().filter(e->e.getTypeLabel().contains(searchText) || e.getTypeCode().contains(searchText)).toList();
        }

        return AjaxResult.ok().data(new PageImpl<>(list));
    }
    @HasPermission("sys-dict:create")
    @GetMapping("type-options")
    public AjaxResult typeOptions(String searchText) {
        List<DictItemVO> list = sysDictService.getAllItems();
        Map<String,String> map = new LinkedHashMap<>();
        for (DictItemVO dictItemDto : list) {
            map.put(dictItemDto.getTypeCode(), dictItemDto.getTypeLabel());
        }
        List<Option> options = new ArrayList<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            options.add(new Option(e.getKey(), e.getValue()));
        }


        if(searchText != null){
            options = options.stream().filter(t->t.getLabel().contains(searchText)).toList();
        }


        return AjaxResult.ok().data(options);
    }

    @Log("字典-创建")
    @HasPermission("sys-dict:create")
    @PostMapping("create")
    public AjaxResult create(@RequestBody SysDictItem param) throws Exception {
        SysDictItem result = itemService.save(param, null);
        return AjaxResult.ok().data(result.getId()).msg("创建成功");
    }

    @Log("字典-更新")
    @HasPermission("sys-dict:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody SysDictItem param, RequestBodyKeys updateFields) throws Exception {
        SysDictItem result = itemService.save(param, updateFields);
        return AjaxResult.ok().data(result.getId()).msg("更新成功");
    }


    @HasPermission("sys-dict:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq idRequest) {
        itemService.deleteById(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }


}
