package io.github.jiangood.openadmin.modules.logviewer.service;

import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import io.github.jiangood.openadmin.modules.logviewer.dto.LogChunk;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class FileLogService {

    /**
     * 单次增量读取的默认上限：日志可能很长，分批发给前端，避免一次分配巨大数组
     */
    public static final int DEFAULT_MAX_LENGTH = 256 * 1024;

    /**
     * 单次增量读取的硬上限，防止调用方传入过大值
     */
    public static final int MAX_LENGTH_LIMIT = 4 * 1024 * 1024;

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
     * 增量读取日志：从 {@code offset} 字节处开始读，最多 {@code maxLength} 字节。
     * <p>
     * 日志文件会随任务持续增长，前端按返回的 {@code nextOffset} 反复调用即可只取新增部分，
     * 避免每次整文件重读（大日志下是 O(n²)）。读取结果按 UTF-8 字符边界切分，
     * 不会把多字节字符截成两半。
     * <p>
     * 文件被重建（长度小于 offset）时自动从头开始读。
     *
     * @param key       日志 key，如 {@code job/{logId}}
     * @param offset    起始字节偏移，&lt;=0 表示从头读
     * @param maxLength 本次最多读取的字节数，&lt;=0 取 {@link #DEFAULT_MAX_LENGTH}
     */
    public LogChunk readChunk(String key, long offset, int maxLength) throws IOException {
        String normalizedKey = normalizeKey(key);
        File file = fileLogConfig.buildLogFile(normalizedKey);

        int size = maxLength <= 0 ? DEFAULT_MAX_LENGTH : Math.min(maxLength, MAX_LENGTH_LIMIT);

        LogChunk chunk = new LogChunk();
        if (!file.isFile()) {
            chunk.setNextOffset(0);
            chunk.setEof(true);
            return chunk;
        }

        long length = file.length();
        long from = offset < 0 ? 0 : offset;
        if (length < from) {
            // 文件被重建（如同一 key 复用），从头再读
            from = 0;
        }

        long remaining = length - from;
        if (remaining <= 0) {
            chunk.setNextOffset(length);
            chunk.setEof(true);
            return chunk;
        }

        int readSize = (int) Math.min(remaining, size);
        byte[] buffer = read(file, from, readSize);
        int end = utf8Boundary(buffer, readSize < remaining);
        if (end <= 0) {
            // offset 落在多字节字符中间（非本服务产生的偏移，如文件被外部改写）：
            // 整批原样解码，保证调用方继续前进，避免前端在同一个 offset 上原地重试
            end = buffer.length;
        }

        chunk.setContent(new String(buffer, 0, end, StandardCharsets.UTF_8));
        chunk.setNextOffset(from + end);
        chunk.setEof(from + end >= length);
        return chunk;
    }

    /**
     * 删除日志文件，文件不存在时忽略
     *
     * @param key 日志 key，如 job/{logId}
     */
    public void deleteLog(String key) throws IOException {
        String normalizedKey = normalizeKey(key);
        Files.deleteIfExists(fileLogConfig.buildLogFile(normalizedKey).toPath());
    }

    private static byte[] read(File file, long from, int size) throws IOException {
        byte[] buffer = new byte[size];
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(from);
            int read = raf.read(buffer);
            if (read <= 0) {
                return new byte[0];
            }
            return read == size ? buffer : java.util.Arrays.copyOf(buffer, read);
        }
    }

    /**
     * 计算本次可安全解码的结束位置（只保留完整 UTF-8 字符）。
     *
     * @param buffer     已读到的字节
     * @param hasMore    文件后续还有内容（本次读满了缓冲区）
     */
    private static int utf8Boundary(byte[] buffer, boolean hasMore) {
        if (!hasMore) {
            // 已读到文件末尾：最后一个字符不会再有后续字节，原样解码
            return buffer.length;
        }
        int i = buffer.length;
        // 跳过尾部续字节（10xxxxxx），让 i 停在最后一个字符的结束位置
        while (i > 0 && (buffer[i - 1] & 0xC0) == 0x80) {
            i--;
        }
        if (i == 0) {
            // 全是续字节：异常数据，原样发出
            return buffer.length;
        }
        int lead = buffer[i - 1] & 0xFF;
        int need = lead < 0x80 ? 1 : lead >= 0xF0 ? 4 : lead >= 0xE0 ? 3 : lead >= 0xC0 ? 2 : 1;
        int start = i - 1;
        // 前导字节之后的字节数不足 need 时说明这个字符没写完整，丢弃这几个字节等下次再读
        return buffer.length - start >= need ? buffer.length : start;
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
