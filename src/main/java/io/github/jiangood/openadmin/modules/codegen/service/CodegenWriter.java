package io.github.jiangood.openadmin.modules.codegen.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.codegen.dto.GeneratedFileVO;
import io.github.jiangood.openadmin.util.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 代码生成写盘：负责路径解析、越界校验与实际写入。
 */
@Component
@RequiredArgsConstructor
public class CodegenWriter {

    private final SystemProperties systemProperties;

    public Path projectRoot() {
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    public Path backendRoot() {
        return root(systemProperties.getCodegen().getBackendDir());
    }

    public Path resourceRoot() {
        return root(systemProperties.getCodegen().getResourceDir());
    }

    public Path frontendRoot() {
        return root(systemProperties.getCodegen().getFrontendDir());
    }

    private Path root(String relativeDir) {
        Path projectRoot = projectRoot();
        Path target = projectRoot.resolve(relativeDir).normalize();
        if (!target.startsWith(projectRoot)) {
            throw new BusinessException("输出目录越界：" + relativeDir);
        }
        return target;
    }

    /**
     * 构造文件描述，不写盘；路径越界时抛出业务异常。
     */
    public GeneratedFileVO file(Path root, String relativePath, String content, String kind) {
        Path absolute = root.resolve(relativePath).normalize();
        if (!absolute.startsWith(root)) {
            throw new BusinessException("非法输出路径：" + relativePath);
        }
        GeneratedFileVO file = new GeneratedFileVO();
        file.setAbsolutePath(absolute.toString());
        file.setPath(projectRoot().relativize(absolute).toString().replace('\\', '/'));
        file.setContent(content);
        file.setKind(kind);
        file.setExists(Files.exists(absolute));
        return file;
    }

    /**
     * 写入文件；目标已存在且不允许覆盖时返回 false。
     */
    public boolean write(GeneratedFileVO file, boolean overwrite) throws IOException {
        Path absolute = Paths.get(file.getAbsolutePath());
        if (Files.exists(absolute) && !overwrite) {
            return false;
        }
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(absolute, file.getContent(), StandardCharsets.UTF_8);
        return true;
    }
}
