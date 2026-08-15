package io.github.jiangood.openadmin.modules.system.controller;

import cn.hutool.core.bean.BeanUtil;
import io.github.jiangood.openadmin.framework.config.RequestBodyKeys;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.Option;
import io.github.jiangood.openadmin.modules.system.dto.DictItemVO;
import io.github.jiangood.openadmin.modules.system.dto.request.DictItemReq;
import io.github.jiangood.openadmin.modules.system.dto.request.DictTypeReq;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import io.github.jiangood.openadmin.modules.system.entity.SysDictType;
import io.github.jiangood.openadmin.modules.system.service.SysDictItemService;
import io.github.jiangood.openadmin.modules.system.service.SysDictService;
import io.github.jiangood.openadmin.modules.system.service.SysDictTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictItemService itemService;
    private final SysDictService sysDictService;
    private final SysDictTypeService sysDictTypeService;

    @HasPermission("sys-dict:read")
    @GetMapping("type-tree")
    public AjaxResult typeTree() {
        return AjaxResult.ok().data(sysDictTypeService.getTypeTree());
    }

    @HasPermission("sys-dict:create")
    @PostMapping("type-create")
    public AjaxResult typeCreate(@RequestBody DictTypeReq req) {
        SysDictType param = BeanUtil.copyProperties(req, SysDictType.class);
        if (sysDictTypeService.isTypeCodeExist(param.getTypeCode(), null)) {
            return AjaxResult.err("类型编码已存在");
        }
        SysDictType result = sysDictTypeService.create(param);
        return AjaxResult.ok().data(result.getId()).msg("创建成功");
    }

    @HasPermission("sys-dict:update")
    @PostMapping("type-update")
    public AjaxResult typeUpdate(@RequestBody DictTypeReq req, RequestBodyKeys updateFields) {
        SysDictType param = BeanUtil.copyProperties(req, SysDictType.class);
        SysDictType result = sysDictTypeService.update(param, updateFields);
        return AjaxResult.ok().data(result.getId()).msg("更新成功");
    }

    @HasPermission("sys-dict:delete")
    @PostMapping("type-delete")
    public AjaxResult typeDelete(@Valid @RequestBody IdReq idRequest) {
        sysDictTypeService.deleteCascade(idRequest.getId());
        return AjaxResult.ok().msg("删除成功");
    }

    @HasPermission("sys-dict:read")
    @GetMapping("type-options")
    public AjaxResult typeOptions(String searchText) {
        List<SysDictType> all = sysDictTypeService.findAll(Sort.by(SysDictType.Fields.seq));
        List<Option> options = all.stream()
                .filter(t -> t.getTypeCode() != null)
                .filter(t -> searchText == null || t.getTypeLabel().contains(searchText))
                .map(t -> new Option(t.getTypeCode(), t.getTypeLabel()))
                .toList();
        return AjaxResult.ok().data(options);
    }

    @HasPermission("sys-dict:read")
    @GetMapping("page")
    public AjaxResult page(String typeCode, String code, String label) {
        List<DictItemVO> list = sysDictService.getAllItems();
        if (typeCode != null) {
            list = list.stream().filter(e -> typeCode.equals(e.getTypeCode())).toList();
        }
        if (code != null) {
            list = list.stream().filter(e -> code.equals(e.getCode())).toList();
        }
        if (label != null) {
            list = list.stream().filter(e -> e.getLabel().contains(label)).toList();
        }
        return AjaxResult.ok().data(new PageImpl<>(list));
    }

    @Log("字典-创建")
    @HasPermission("sys-dict:create")
    @PostMapping("create")
    public AjaxResult create(@RequestBody DictItemReq req) {
        SysDictItem param = BeanUtil.copyProperties(req, SysDictItem.class);
        SysDictItem result = itemService.save(param, null);
        return AjaxResult.ok().data(result.getId()).msg("创建成功");
    }

    @Log("字典-更新")
    @HasPermission("sys-dict:update")
    @PostMapping("update")
    public AjaxResult update(@RequestBody DictItemReq req, RequestBodyKeys updateFields) {
        SysDictItem param = BeanUtil.copyProperties(req, SysDictItem.class);
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
