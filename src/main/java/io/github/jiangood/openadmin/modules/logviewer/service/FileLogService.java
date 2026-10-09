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
        String normalizedKey = normalizeKey(key);
        File file = fileLogConfig.buildLogFile(normalizedKey);

        if (!file.exists()) {
            return "文件不存在:" + file.getAbsolutePath();
        }

        try (FileInputStream is = new FileInputStream(file)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    /**
     * 归一化并校验 key：控制器用 {@code {*key}} 捕获路径，捕获值带前导 {@code /}
     * （如 {@code /job/123}），需先去掉再校验、拼接文件；同时拦截空路径段与
     * {@code .}/{@code ..} 以阻止路径穿越。
     */
    private String normalizeKey(String key) throws IOException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("非法的日志文件 key");
        }

        String normalized = key;
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("非法的日志文件 key: " + key);
        }

        for (String segment : normalized.split("/")) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                throw new IllegalArgumentException("非法的日志文件 key: " + key);
            }
        }

        String canonicalPath = fileLogConfig.buildLogFile(normalized).getCanonicalPath();
        String basePath = new File(fileLogConfig.getLogPath()).getCanonicalPath();
        Assert.state(canonicalPath.startsWith(basePath + File.separator),
                "非法的日志文件 key: " + key);
        return normalized;
    }
}
