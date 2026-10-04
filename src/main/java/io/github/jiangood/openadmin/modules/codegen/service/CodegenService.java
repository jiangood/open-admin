package io.github.jiangood.openadmin.modules.codegen.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.codegen.dto.CodegenReq;
import io.github.jiangood.openadmin.modules.codegen.dto.CodegenResultVO;
import io.github.jiangood.openadmin.modules.codegen.dto.EntityMetaVO;
import io.github.jiangood.openadmin.modules.codegen.dto.FieldMetaVO;
import io.github.jiangood.openadmin.modules.codegen.dto.GeneratedFileVO;
import io.github.jiangood.openadmin.modules.codegen.template.BackendTemplates;
import io.github.jiangood.openadmin.modules.codegen.template.FrontendTemplates;
import io.github.jiangood.openadmin.modules.codegen.template.MenuTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成编排：扫描实体 → 生成各层文件 → 预览或写入。
 */
@Service
@RequiredArgsConstructor
public class CodegenService {

    private final EntityScanner entityScanner;
    private final CodegenWriter writer;
    private final SystemProperties systemProperties;

    public List<EntityMetaVO> listEntities() {
        return entityScanner.scan();
    }

    public EntityMetaVO getEntity(String className) {
        return entityScanner.get(className);
    }

    public List<GeneratedFileVO> preview(CodegenReq req) {
        return buildFiles(req);
    }

    public CodegenResultVO generate(CodegenReq req) throws IOException {
        List<GeneratedFileVO> files = buildFiles(req);
        CodegenResultVO result = new CodegenResultVO();
        result.setFiles(files);
        for (GeneratedFileVO file : files) {
            if (writer.write(file, req.isOverwrite())) {
                result.getWritten().add(file.getPath());
            } else {
                result.getSkipped().add(file.getPath());
            }
        }
        return result;
    }

    private List<GeneratedFileVO> buildFiles(CodegenReq req) {
        EntityMetaVO meta = entityScanner.get(req.getClassName());
        String module = StringUtils.hasText(req.getModule()) ? req.getModule() : meta.getModule();
        String label = StringUtils.hasText(req.getLabel()) ? req.getLabel() : meta.getLabel();
        String parentMenu = StringUtils.hasText(req.getParentMenu()) ? req.getParentMenu() : "sys";
        String entity = meta.getSimpleName();
        String modulePackage = meta.getModulePackage();
        String packagePath = modulePackage.replace('.', '/');

        boolean hasFile = meta.getFields().stream().anyMatch(FieldMetaVO::isFile);
        String frontendImport = resolveFrontendImport(module);

        Path backendRoot = writer.backendRoot();
        Path resourceRoot = writer.resourceRoot();
        Path frontendRoot = writer.frontendRoot();

        List<GeneratedFileVO> files = new ArrayList<>();
        files.add(writer.file(backendRoot, packagePath + "/repository/" + entity + "Repository.java",
                BackendTemplates.repository(meta), "backend"));
        files.add(writer.file(backendRoot, packagePath + "/service/" + entity + "Service.java",
                BackendTemplates.service(meta, hasFile), "backend"));
        files.add(writer.file(backendRoot, packagePath + "/controller/" + entity + "Controller.java",
                BackendTemplates.controller(meta, module, label), "backend"));
        files.add(writer.file(frontendRoot, module + "/index.jsx",
                FrontendTemplates.page(meta, module, label, frontendImport), "frontend"));
        files.add(writer.file(resourceRoot, "application-menu-" + module + ".yml",
                MenuTemplate.menu(module, label, parentMenu), "menu"));
        return files;
    }

    private String resolveFrontendImport(String module) {
        String configured = systemProperties.getCodegen().getFrontendImport();
        if (StringUtils.hasText(configured)) {
            return configured;
        }
        Path projectRoot = writer.projectRoot();
        Path frameworkDir = projectRoot.resolve("web/src/framework");
        if (Files.exists(frameworkDir.resolve("index.ts"))) {
            Path pageDir = writer.frontendRoot().resolve(module);
            return pageDir.relativize(frameworkDir).toString().replace('\\', '/');
        }
        return "@jiangood/open-admin";
    }
}
