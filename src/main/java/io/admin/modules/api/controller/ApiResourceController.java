package io.admin.modules.api.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.modules.api.service.ApiResourceService;
import io.admin.common.dto.table.Table;
import io.admin.framework.data.query.JpaQuery;
import io.admin.common.dto.AjaxResult;
import io.admin.modules.api.entity.ApiResource;
import io.admin.common.dto.DropdownRequest;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/api/resource")
public class ApiResourceController {
    @Resource
    private ApiResourceService service;

    @HasPermission("api")
    @RequestMapping("page")
    public AjaxResult page(String searchText) throws Exception {
        List<ApiResource> list = service.findAll();
        list = list.stream().filter(r -> {
            if (StrUtil.isNotEmpty(searchText)) {
                return r.getName().contains(searchText) ||
                        r.getAction().contains(searchText) ||
                        (r.getDesc() != null && r.getDesc().contains(searchText))
                        ;
            }
            return true;
        }).toList();

        return AjaxResult.ok().data(new PageImpl<>(list));
    }


}
