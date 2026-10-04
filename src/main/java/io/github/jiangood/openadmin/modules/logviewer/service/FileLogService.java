package io.github.jiangood.openadmin.modules.logviewer.service;

import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class FileLogService {

    @Resource
    private FileLogConfig fileLogConfig;

    public String readLogContent(String key) throws IOException {
        validateKey(key);
        File file = fileLogConfig.buildLogFile(key);

        if (!file.exists()) {
            return "文件不存在:" + file.getAbsolutePath();
        }

        try (FileInputStream is = new FileInputStream(file)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    private void validateKey(String key) throws IOException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("非法的日志文件 key");
        }
        for (String segment : key.split("/")) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                throw new IllegalArgumentException("非法的日志文件 key: " + key);
            }
        }

        String canonicalPath = fileLogConfig.buildLogFile(key).getCanonicalPath();
        String basePath = new File(fileLogConfig.getLogPath()).getCanonicalPath();
        Assert.state(canonicalPath.startsWith(basePath + File.separator),
                "非法的日志文件 key: " + key);
    }
}
