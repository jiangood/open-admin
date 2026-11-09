package io.github.jiangood.openadmin.modules.system.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.util.FileTool;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class LocalFileOperator implements FileOperator {

    private final Path root;

    public LocalFileOperator(String savePath) {
        log.info("本地文件保存地址为: {}", savePath);
        FileUtil.mkdir(savePath);
        this.root = Path.of(savePath).toAbsolutePath().normalize();
    }


    @Override
    public void save(String key, InputStream inputStream) {
        // 文件管理
        Path file = resolve(key);
        FileUtil.writeFromStream(inputStream, file.toFile());
    }

    @Override
    public void saveFile(String key, File file) throws IOException {
        Path target = resolve(key);
        FileUtil.copyFile(file, target.toFile());
    }

    /**
     * 解析 key 到根目录下；越界（目录穿越/绝对路径）直接拒绝，避免读写到根目录之外。
     */
    private Path resolve(String key) {
        Path path = FileTool.resolveUnder(root, key);
        if (path == null) {
            throw new IllegalArgumentException("非法的文件路径: " + key);
        }
        return path;
    }

    @Override
    public InputStream getFileStream(String key) throws IOException {
        Path path = resolve(key);
        if (!Files.isRegularFile(path)) {
            String message = CharSequenceUtil.format("本地文件不存在,key={} ,path={}", key, path);
            throw new FileNotFoundException(message);
        }
        return Files.newInputStream(path);
    }

    @Override
    public void downloadFile(String key, File target) throws IOException {
        FileUtil.copyFile(resolve(key).toFile(), target);
    }


    @Override
    public void delete(String key) {
        // 越界路径直接拒绝，不执行删除
        FileUtil.del(resolve(key).toFile());
    }

    @Override
    public boolean exist(String key) {
        Path path = FileTool.resolveUnder(root, key);
        boolean exist = path != null && Files.isRegularFile(path);
        log.trace("判断文件是否存在 {} {}", path, exist);
        return exist;
    }
}
