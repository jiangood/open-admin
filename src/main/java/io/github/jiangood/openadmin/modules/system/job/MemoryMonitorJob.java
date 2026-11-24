package io.github.jiangood.openadmin.modules.system.job;

import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.modules.job.JobResult;
import io.github.jiangood.openadmin.util.field.FieldDescription;
import io.github.jiangood.openadmin.util.field.ValueType;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * 内存监控演示任务：单次执行内循环打印内存使用率（JVM 堆 + 系统内存），带序号。
 * <p>
 * 默认 100 次 × 5 秒 ≈ 8 分钟，便于联调任务运行状态展示、运行中取消（行内「取消」按钮）
 * 与执行日志查看；可在任务参数中调整次数与间隔，执行中随时取消。
 * <p>
 * 未声明 defaultCron：导入后默认停用，通过「执行一次」手动触发测试，需要定时执行时再编辑 cron。
 */
@Component
@DisallowConcurrentExecution
@JobDescription(
        label = "内存监控",
        params = {
                @FieldDescription(name = "total", label = "打印次数", type = ValueType.NUMBER, defaultValue = "100"),
                @FieldDescription(name = "intervalSeconds", label = "打印间隔（秒）", type = ValueType.NUMBER, defaultValue = "5"),
        })
public class MemoryMonitorJob extends BaseJob {

    @Override
    public JobResult execute(JobDataMap data, Logger logger) throws Exception {
        int total = readInt(data, "total", 100, 1, 10_000);
        int intervalSeconds = readInt(data, "intervalSeconds", 5, 1, 3_600);

        for (int i = 1; i <= total; i++) {
            // 先打印再休眠：第 1 次立即输出；取消时 sleep 抛出 InterruptedException，
            // 由 BaseJob 统一按「已取消」记录，无需在此捕获
            logger.info("#{} 内存使用率：{}", i, memoryText());
            if (i < total) {
                Thread.sleep(intervalSeconds * 1000L);
            }
        }
        return JobResult.ok("已打印 " + total + " 次内存使用率（间隔 " + intervalSeconds + " 秒）");
    }

    /**
     * 组装一行内存描述：JVM 堆使用率 + 系统内存使用率（系统内存不可用时只输出 JVM 堆）
     */
    private String memoryText() {
        Runtime rt = Runtime.getRuntime();
        long usedHeap = rt.totalMemory() - rt.freeMemory();
        long maxHeap = rt.maxMemory();
        double heapPct = maxHeap > 0 ? usedHeap * 100.0 / maxHeap : 0;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("JVM堆 %.1f%%（%s/%s）", heapPct, formatSize(usedHeap), formatSize(maxHeap)));

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof com.sun.management.OperatingSystemMXBean sunOs && sunOs.getTotalMemorySize() > 0) {
            long totalMem = sunOs.getTotalMemorySize();
            long freeMem = sunOs.getFreeMemorySize();
            sb.append(String.format("，系统 %.1f%%（%s/%s，可用 %s）",
                    (totalMem - freeMem) * 100.0 / totalMem,
                    formatSize(totalMem - freeMem), formatSize(totalMem), formatSize(freeMem)));
        }
        return sb.toString();
    }

    private static String formatSize(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        return mb >= 1024 ? String.format("%.1f GB", mb / 1024) : String.format("%.0f MB", mb);
    }

    /**
     * 从任务参数读取整数（兼容数字与字符串），越界或非法时回退默认值并收敛到 [min, max]
     */
    private static int readInt(JobDataMap data, String key, int defaultValue, int min, int max) {
        Object value = data.get(key);
        int n = defaultValue;
        if (value instanceof Number number) {
            n = number.intValue();
        } else if (value instanceof String s && !s.isBlank()) {
            try {
                n = Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return Math.max(min, Math.min(n, max));
    }
}
