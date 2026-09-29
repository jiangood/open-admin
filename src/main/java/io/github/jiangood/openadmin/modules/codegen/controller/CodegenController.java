package io.github.jiangood.openadmin.modules.codegen.controller;

import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.modules.codegen.dto.CodegenReq;
import io.github.jiangood.openadmin.modules.codegen.dto.CodegenResultVO;
import io.github.jiangood.openadmin.modules.codegen.service.CodegenService;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.Option;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 代码生成：动态扫描实体，按实体生成常见 CRUD 代码并写入项目源码目录。
 */
@RestController
@RequestMapping("admin/codegen")
@RequiredArgsConstructor
public class CodegenController {

    private final CodegenService codegenService;

    /**
     * 动态扫描的实体列表，供前端下拉选择。
     */
    @HasPermission("sys-codegen:read")
    @GetMapping("entity-options")
    public AjaxResult entityOptions() {
        List<Option> options = codegenService.listEntities().stream().map(meta -> {
            Option option = new Option();
            option.setValue(meta.getClassName());
            option.setLabel(meta.getLabel() + "（" + meta.getSimpleName() + "）");
            option.setData(Map.of(
                    "module", meta.getModule(),
                    "label", meta.getLabel(),
                    "frameworkEntity", meta.isFrameworkEntity(),
                    "packageName", meta.getPackageName()));
            return option;
        }).toList();
        return AjaxResult.ok().data(options);
    }

    /**
     * 单个实体的字段元数据。
     */
    @HasPermission("sys-codegen:read")
    @GetMapping("entity-info")
    public AjaxResult entityInfo(@RequestParam String className) {
        return AjaxResult.ok().data(codegenService.getEntity(className));
    }

    /**
     * 预览将生成的文件，不写盘。
     */
    @Log("代码生成-预览")
    @HasPermission("sys-codegen:generate")
    @PostMapping("preview")
    public AjaxResult preview(@Valid @RequestBody CodegenReq req) {
        return AjaxResult.ok().data(codegenService.preview(req));
    }

    /**
     * 生成并写入项目源码目录。
     */
    @Log("代码生成-生成")
    @HasPermission("sys-codegen:generate")
    @PostMapping("generate")
    public AjaxResult generate(@Valid @RequestBody CodegenReq req) throws IOException {
        CodegenResultVO result = codegenService.generate(req);
        return AjaxResult.ok()
                .data(result)
                .msg("生成完成：写入 " + result.getWritten().size() + " 个，跳过 " + result.getSkipped().size() + " 个");
    }
}
