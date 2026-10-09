package io.github.jiangood.openadmin.modules.codegen.template;

import io.github.jiangood.openadmin.modules.codegen.dto.EntityMetaVO;
import io.github.jiangood.openadmin.modules.codegen.dto.FieldMetaVO;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后端 CRUD 代码模板（Java 21 文本块）。
 */
public final class BackendTemplates {

    private BackendTemplates() {
    }

    public static String repository(EntityMetaVO meta) {
        String entity = meta.getSimpleName();
        return """
                package %s.repository;

                import %s;
                import io.github.jiangood.openadmin.framework.data.BaseRepository;
                import org.springframework.stereotype.Repository;

                @Repository
                public interface %sRepository extends BaseRepository<%s, String> {
                }
                """.formatted(meta.getModulePackage(), meta.getClassName(), entity, entity);
    }

    public static String service(EntityMetaVO meta, boolean hasFile) {
        String entity = meta.getSimpleName();
        String repositoryField = lowerFirst(entity) + "Repository";
        if (!hasFile) {
            return """
                    package %s.service;

                    import %s;
                    import io.github.jiangood.openadmin.framework.data.BaseService;
                    import org.springframework.stereotype.Service;
                    import org.springframework.transaction.annotation.Transactional;

                    import java.util.List;

                    @Service
                    public class %sService extends BaseService<%s> {

                        @Transactional
                        public %s save(%s input, List<String> requestKeys) {
                            if (input.isNew()) {
                                return repository.save(input);
                            }
                            this.updateField(input, requestKeys);
                            return repository.findById(input.getId()).orElse(null);
                        }
                    }
                    """.formatted(meta.getModulePackage(), meta.getClassName(), entity, entity, entity, entity);
        }
        return """
                package %s.service;

                import %s;
                import %s.repository.%sRepository;
                import io.github.jiangood.openadmin.framework.data.BaseService;
                import io.github.jiangood.openadmin.modules.system.service.SysFileService;
                import lombok.RequiredArgsConstructor;
                import org.springframework.stereotype.Service;
                import org.springframework.transaction.annotation.Transactional;
                import org.springframework.util.Assert;

                import java.util.List;

                @RequiredArgsConstructor
                @Service
                public class %sService extends BaseService<%s> {

                    private final %sRepository %s;
                    private final SysFileService sysFileService;

                    @Transactional
                    public %s save(%s input, List<String> requestKeys) {
                        if (input.isNew()) {
                            %s result = %s.save(input);
                            sysFileService.claim(result);
                            return result;
                        }
                        %s old = %s.findById(input.getId()).orElse(null);
                        Assert.notNull(old, "数据不存在");
                        sysFileService.unclaim(old);
                        this.updateField(input, requestKeys);
                        %s.flush();
                        sysFileService.claim(input);
                        return %s.findById(input.getId()).orElse(null);
                    }

                    @Transactional
                    @Override
                    public void deleteById(String id) {
                        %s entity = %s.findById(id).orElse(null);
                        if (entity == null) {
                            return;
                        }
                        sysFileService.unclaim(entity);
                        super.deleteById(id);
                    }
                }
                """.formatted(meta.getModulePackage(), meta.getClassName(), meta.getModulePackage(), entity,
                entity, entity, entity, repositoryField,
                entity, entity, entity, repositoryField,
                entity, repositoryField,
                repositoryField, repositoryField,
                entity, repositoryField);
    }

    public static String controller(EntityMetaVO meta, String module, String label) {
        String entity = meta.getSimpleName();
        String serviceType = entity + "Service";
        String importBase = """
                import %s;
                import %s.service.%s;
                """.formatted(meta.getClassName(), meta.getModulePackage(), serviceType);

        List<FieldMetaVO> stringFields = searchable(meta).stream()
                .filter(f -> "STRING".equals(f.getCategory()))
                .toList();
        List<FieldMetaVO> filterFields = searchable(meta).stream()
                .filter(f -> "ENUM".equals(f.getCategory()) || "BOOLEAN".equals(f.getCategory()))
                .toList();

        StringBuilder imports = new StringBuilder(importBase);
        Set<String> extraImports = new LinkedHashSet<>();
        for (FieldMetaVO field : filterFields) {
            if ("ENUM".equals(field.getCategory())) {
                extraImports.add(field.getTypeFqn());
            }
        }
        for (String extra : extraImports) {
            imports.append("import ").append(extra).append(";\n");
        }

        StringBuilder params = new StringBuilder();
        if (!stringFields.isEmpty()) {
            params.append("String searchText, ");
        }
        for (FieldMetaVO field : filterFields) {
            params.append(paramType(field)).append(' ').append(field.getName()).append(", ");
        }

        StringBuilder pageBody = new StringBuilder();
        pageBody.append("        var spec = service.spec();\n");
        if (!stringFields.isEmpty()) {
            pageBody.append("        spec.orLike(searchText, ")
                    .append(stringFields.stream().map(f -> "\"" + f.getName() + "\"")
                            .collect(Collectors.joining(", ")))
                    .append(");\n");
        }
        for (FieldMetaVO field : filterFields) {
            pageBody.append("        spec.eq(\"").append(field.getName()).append("\", ")
                    .append(field.getName()).append(");\n");
        }
        pageBody.append("        var page = service.findAll(spec, pageable);\n");
        pageBody.append("        return AjaxResult.ok().data(page);");

        return """
                package %s.controller;

                %simport io.github.jiangood.openadmin.framework.log.Log;
                import io.github.jiangood.openadmin.framework.perm.HasPermission;
                import io.github.jiangood.openadmin.util.dto.AjaxResult;
                import io.github.jiangood.openadmin.util.dto.IdReq;
                import jakarta.validation.Valid;
                import lombok.RequiredArgsConstructor;
                import org.springframework.data.domain.Pageable;
                import org.springframework.data.domain.Sort;
                import org.springframework.data.web.PageableDefault;
                import org.springframework.web.bind.annotation.*;

                import java.util.List;

                @RestController
                @RequestMapping("admin/%s")
                @RequiredArgsConstructor
                public class %sController {

                    private final %s service;

                    @HasPermission("%s:read")
                    @GetMapping("page")
                    public AjaxResult page(%s@PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
                %s
                    }

                    @HasPermission("%s:read")
                    @GetMapping("info/{id}")
                    public AjaxResult info(@PathVariable String id) {
                        return service.findById(id)
                                .map(data -> AjaxResult.ok().data(data))
                                .orElse(AjaxResult.err().msg("记录不存在"));
                    }

                    @Log("%s-创建")
                    @HasPermission("%s:create")
                    @PostMapping("create")
                    public AjaxResult create(@RequestBody %s input) throws Exception {
                        service.save(input, null);
                        return AjaxResult.ok().msg("创建成功");
                    }

                    @Log("%s-更新")
                    @HasPermission("%s:update")
                    @PostMapping("update")
                    public AjaxResult update(@RequestBody %s input, @RequestHeader("X-Body-Fields") List<String> updateFields) throws Exception {
                        service.save(input, updateFields);
                        return AjaxResult.ok().msg("更新成功");
                    }

                    @Log("%s-删除")
                    @HasPermission("%s:delete")
                    @PostMapping("delete")
                    public AjaxResult delete(@Valid @RequestBody IdReq req) {
                        service.deleteById(req.getId());
                        return AjaxResult.ok().msg("删除成功");
                    }
                }
                """.formatted(meta.getModulePackage(), imports, module, entity, serviceType,
                module, params,
                pageBody,
                module,
                label, module, entity,
                label, module, entity,
                label, module);
    }

    private static List<FieldMetaVO> searchable(EntityMetaVO meta) {
        List<FieldMetaVO> list = new ArrayList<>();
        for (FieldMetaVO field : meta.getFields()) {
            if (field.isSearchable()) {
                list.add(field);
            }
        }
        return list;
    }

    private static String paramType(FieldMetaVO field) {
        if ("ENUM".equals(field.getCategory())) {
            return field.getType();
        }
        return "Boolean";
    }

    private static String lowerFirst(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }
}
