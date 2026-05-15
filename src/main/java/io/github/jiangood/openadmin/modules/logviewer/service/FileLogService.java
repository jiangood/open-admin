package io.github.jiangood.openadmin.modules.logviewer.service;

import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class FileLogService {

    @Resource
    private FileLogConfig fileLogConfig;

    public String readLogContent(String key) throws IOException {
        File file = fileLogConfig.buildLogFile(key);

        if (!file.exists()) {
            return "文件不存在:" + file.getAbsolutePath();
        }

        try (FileInputStream is = new FileInputStream(file)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
