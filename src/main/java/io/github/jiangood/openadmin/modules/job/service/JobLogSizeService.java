package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.dto.LogSizeStat;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.logviewer.service.FileLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 任务日志文件占用统计
 * <p>
 * 日志写在 {@code ${logging.file.path}/job/{logId}.log}，这里只读扫描该目录，并做短时缓存
 * （默认 30 秒），避免管理页轮询时反复 stat 大量文件。文件名与 {@code sys_job_log.id} 一一对应，
 * 因此不需要额外索引即可按任务汇总。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobLogSizeService {

    private static final String JOB_LOG_DIR = "job";

    /**
     * 统计结果缓存时长，清理/删除后调用 {@link #invalidate()} 可立即失效
     */
    private static final long CACHE_MILLIS = 30_000L;

    private static final long BYTES_PER_MB = 1024L * 1024L;

    private final FileLogService fileLogService;
    private final SysJobLogRepository sysJobLogRepository;
    private final SystemProperties systemProperties;

    private volatile Snapshot cached;

    /**
     * 全部任务日志的占用情况
     */
    public LogSizeStat total() {
        Snapshot snapshot = snapshot();

        LogSizeStat stat = new LogSizeStat();
        stat.setBytes(snapshot.totalBytes);
        stat.setFiles(snapshot.sizeByLogId.size());

        int warnMb = systemProperties.getJobLogSizeWarnMb();
        if (warnMb > 0) {
            stat.setWarnMb((long) warnMb);
            stat.setWarn(snapshot.totalBytes >= warnMb * BYTES_PER_MB);
        }
        return stat;
    }

    /**
     * 单个任务的日志占用：按该任务的执行记录 ID 关联文件
     */
    public LogSizeStat forJob(String jobId) {
        Snapshot snapshot = snapshot();

        long bytes = 0;
        long files = 0;
        List<String> logIds = sysJobLogRepository.findIdsByJobId(jobId);
        for (String logId : logIds) {
            Long size = snapshot.sizeByLogId.get(logId);
            if (size != null) {
                bytes += size;
                files++;
            }
        }

        LogSizeStat stat = new LogSizeStat();
        stat.setBytes(bytes);
        stat.setFiles(files);
        return stat;
    }

    /**
     * 清理或删除日志文件后主动失效缓存，让下次统计立即反映磁盘变化
     */
    public void invalidate() {
        cached = null;
    }

    private Snapshot snapshot() {
        Snapshot current = cached;
        if (isFresh(current)) {
            return current;
        }
        synchronized (this) {
            if (isFresh(cached)) {
                return cached;
            }
            cached = scan();
            return cached;
        }
    }

    private static boolean isFresh(Snapshot snapshot) {
        return snapshot != null && System.currentTimeMillis() - snapshot.createdAt < CACHE_MILLIS;
    }

    private Snapshot scan() {
        Map<String, Long> sizeByLogId;
        try {
            sizeByLogId = fileLogService.statLogFiles(JOB_LOG_DIR);
        } catch (IOException e) {
            log.warn("统计任务日志文件占用失败: {}", e.getMessage());
            sizeByLogId = Collections.emptyMap();
        }
        long totalBytes = sizeByLogId.values().stream().mapToLong(Long::longValue).sum();
        return new Snapshot(System.currentTimeMillis(), totalBytes, sizeByLogId);
    }

    private static final class Snapshot {

        private final long createdAt;
        private final long totalBytes;
        private final Map<String, Long> sizeByLogId;

        private Snapshot(long createdAt, long totalBytes, Map<String, Long> sizeByLogId) {
            this.createdAt = createdAt;
            this.totalBytes = totalBytes;
            this.sizeByLogId = sizeByLogId;
        }
    }
}
