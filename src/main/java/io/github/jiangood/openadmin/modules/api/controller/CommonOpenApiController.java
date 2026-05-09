package io.github.jiangood.openadmin.modules.api.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.api.ApiResult;
import io.github.jiangood.openadmin.modules.system.dto.DictItemVO;
import io.github.jiangood.openadmin.modules.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/gateway")
@RequiredArgsConstructor
public class CommonOpenApiController {

    private final SysDictService sysDictService;
    @HasPermission("common:dict")
    @Operation(operationId = "common:dict", summary = "获得数据字典项", description = "根据字典类型编码，获得对应的项目")
    @GetMapping("/dict")
    public ApiResult<List<DictItemVO>> dict(@Parameter(description = "字典类型编码" ) @RequestParam String typeCode) {
        List<DictItemVO> list = sysDictService.getAllItems();
        List<DictItemVO> rs = list.stream().filter(t -> t.getTypeCode().equalsIgnoreCase(typeCode)).toList();
        return ApiResult.ok(rs);
    }




}
