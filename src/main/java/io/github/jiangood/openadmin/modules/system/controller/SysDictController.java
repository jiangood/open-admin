package io.github.jiangood.openadmin.modules.system.controller;

import io.github.jiangood.openadmin.framework.config.argument.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.config.datadefinition.DictDefinition;
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.lang.dto.IdRequest;
import io.github.jiangood.openadmin.lang.dto.antd.Option;
import io.github.jiangood.openadmin.modules.system.dto.DictItemDto;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.service.SysDictItemService;
import io.github.jiangood.openadmin.modules.system.service.SysDictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("admin/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictItemService itemService;
    private final SysDictService sysDictService;




    @PreAuthorize("hasAuthority('sysDict:view')")
    @RequestMapping("page")
    public AjaxResult page(String searchText) {
        List<DictItemDto> list = sysDictService.getAllItems();
        if(searchText != null){
            list = list.stream().filter(e->e.getTypeLabel().contains(searchText) || e.getTypeCode().contains(searchText)).toList();
        }

        return AjaxResult.ok().data(new PageImpl<>(list));
    }
    @PreAuthorize("hasAuthority('sysDict:save')")
    @GetMapping("typeOptions")
    public AjaxResult typeOptions(String searchText) {
        List<DictItemDto> list = sysDictService.getAllItems();
        Map<String,String> map = new LinkedHashMap<>();
        for (DictItemDto dictItemDto : list) {
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

    @PreAuthorize("hasAuthority('sysDict:save')")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysDictItem param, RequestBodyKeys updateFields) throws Exception {
        SysDictItem result = itemService.save(param, updateFields);
        return AjaxResult.ok().data(result.getId()).msg("保存成功");
    }


    @PreAuthorize("hasAuthority('sysDict:delete')")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdRequest idRequest) {
        itemService.delete(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }


}
