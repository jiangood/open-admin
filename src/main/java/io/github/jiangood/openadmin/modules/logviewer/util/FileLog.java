package io.github.jiangood.openadmin.modules.logviewer.util;

import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 文件日志：写入 {@code ${logging.file.path}/{key}.log} 的日志，供前端日志弹窗查看。
 * <p>
 * 写入哪个文件只取决于当前线程绑定的 key，与包名 / 类名无关：
 * <ul>
 *   <li>{@link #logger()}：取文件日志 logger（固定名 {@link FileLogConfig#LOGGER_NAME}）</li>
 *   <li>{@link #bind(String)} / {@link #unbind()} / {@link #currentKey()}：绑定 / 解绑当前线程的日志文件</li>
 * </ul>
 * 与 {@link FileLogConfig} 的对应关系：该配置为 {@code FILE-LOGGER} 挂载按
 * {@link FileLogConfig#DISCRIMINATOR_KEY} 分流的 {@code SiftingAppender}。
 * <p>
 * 典型用法：
 * <pre>{@code
 * FileLog.bind("order/" + orderId);
 * try {
 *     FileLog.logger().info("开始处理 {}", orderId);
 * } finally {
 *     FileLog.unbind();
 * }
 * }</pre>
 */
public class FileLog {
    private FileLog() {
    }

    /**
     * 文件日志 logger。线程内已 {@link #bind(String)} 时写入对应日志文件；
     * 未绑定时只进控制台 / 应用主日志。
     */
    public static Logger logger() {
        return LoggerFactory.getLogger(FileLogConfig.LOGGER_NAME);
    }

    /**
     * 把当前线程绑定到 {@code key} 对应的日志文件。
     *
     * @param key 日志标识，即日志文件名（不含 .log），如 {@code job/{logId}}
     */
    public static void bind(String key) {
        MDC.put(FileLogConfig.DISCRIMINATOR_KEY, key);
    }

    /**
     * 解绑当前线程的日志文件，避免线程复用（如 {@code @Async} 线程池）时串日志。
     * <p>
     * 只移除本类写入的 MDC key，不清空其它字段（如 {@code traceId}）。
     */
    public static void unbind() {
        MDC.remove(FileLogConfig.DISCRIMINATOR_KEY);
    }

    /**
     * 当前线程绑定的日志 key；未绑定时返回 null。
     */
    public static String currentKey() {
        return MDC.get(FileLogConfig.DISCRIMINATOR_KEY);
    }
}
