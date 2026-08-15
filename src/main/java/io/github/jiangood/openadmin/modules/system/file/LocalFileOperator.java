package io.github.jiangood.openadmin.modules.system.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.jiangood.openadmin.framework.spi.FileOperator;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class LocalFileOperator implements FileOperator {

    private final String dir;

    public LocalFileOperator(String savePath) {
        log.info("本地文件保存地址为: {}", savePath);
        FileUtil.mkdir(savePath);
        this.dir = savePath + File.separator;
    }


    @Override
    public void save(String key, InputStream inputStream) {
        // 文件管理
        String absoluteFile = getAbsoluteFile(key);
        FileUtil.writeFromStream(inputStream, absoluteFile);
    }

    @Override
    public void saveFile(String key, File file) throws IOException {
        String absoluteFile = getAbsoluteFile(key);
        FileUtil.copyFile(file, new File(absoluteFile));
    }

    private String getAbsoluteFile(String key) {
        return dir + key;
    }

    @Override
    public InputStream getFileStream(String key) throws IOException {
        // 判断文件存在不存在
        String absoluteFile = getAbsoluteFile(key);
        if (!FileUtil.exist(absoluteFile)) {
            String message = CharSequenceUtil.format("本地文件不存在,key={} ,path={}", key, absoluteFile);
            throw new FileNotFoundException(message);
        }
        return FileUtil.getInputStream(absoluteFile);
    }

    @Override
    public void downloadFile(String key, File target) throws IOException {
        // 判断文件存在不存在
        String absoluteFile = getAbsoluteFile(key);
        FileUtil.copyFile(new File(absoluteFile), target);
    }


    @Override
    public void delete(String key) {
        // 判断文件存在不存在
        String file = getAbsoluteFile(key);
        if (!FileUtil.exist(file)) {
            return;
        }

        // 删除文件
        FileUtil.del(file);
    }

    @Override
    public boolean exist(String key) {
        String absoluteFile = getAbsoluteFile(key);
        boolean exist = FileUtil.exist(absoluteFile);
        log.trace("判单文件是否存在 {} {}", absoluteFile, exist);
        return exist;
    }
}
