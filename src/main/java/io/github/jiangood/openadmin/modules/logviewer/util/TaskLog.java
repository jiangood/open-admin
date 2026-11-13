package io.github.jiangood.openadmin.modules.logviewer.util;

import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 任务/运行日志：写入 {@code ${logging.file.path}/{key}.log} 的日志，供前端日志查看。
 * <p>
 * 写入哪个文件只取决于当前线程绑定的 key，与包名/类名无关。支持静态调用：
 * <pre>{@code
 * TaskLog.bind("task/" + logId);
 * try {
 *     TaskLog.info("开始处理 {}", id);
 * } finally {
 *     TaskLog.unbind();
 * }
 * }</pre>
 * 嵌套绑定时保留 {@link #bind(String)} 返回值，内层结束用 {@link #unbind(String)} 恢复外层 key。
 * <p>
 * 仍保留 {@link #logger()} 供需要持有 Logger 引用或跨线程传递的场景。
 */
public final class TaskLog {

    private TaskLog() {
    }

    // --------------------------------------------------------------- 绑定/上下文

    /**
     * 文件日志 logger。线程内已 {@link #bind(String)} 时写入对应日志文件；
     * 未绑定时只进控制台/应用主日志。
     */
    public static Logger logger() {
        return LoggerFactory.getLogger(FileLogConfig.LOGGER_NAME);
    }

    /**
     * 把当前线程绑定到 {@code key} 对应的日志文件。
     *
     * @param key 日志标识，即日志文件名（不含 .log），如 {@code task/{logId}}、{@code job/{logId}}
     * @return 绑定前的 key（没有绑定时为 {@code null}），嵌套绑定后可用
     * {@link #unbind(String)} 恢复外层绑定
     */
    public static String bind(String key) {
        String previous = MDC.get(FileLogConfig.DISCRIMINATOR_KEY);
        MDC.put(FileLogConfig.DISCRIMINATOR_KEY, key);
        return previous;
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
     * 恢复到 {@link #bind(String)} 之前的绑定，用于嵌套场景。
     *
     * @param previous {@link #bind(String)} 的返回值；为 {@code null} 时等同于 {@link #unbind()}
     */
    public static void unbind(String previous) {
        if (previous == null) {
            unbind();
            return;
        }
        MDC.put(FileLogConfig.DISCRIMINATOR_KEY, previous);
    }

    /**
     * 当前线程绑定的日志 key；未绑定时返回 null。
     */
    public static String currentKey() {
        return MDC.get(FileLogConfig.DISCRIMINATOR_KEY);
    }

    // --------------------------------------------------------------- 静态日志方法

    public static void trace(String msg) {
        logger().trace(msg);
    }

    public static void trace(String format, Object arg) {
        logger().trace(format, arg);
    }

    public static void trace(String format, Object arg1, Object arg2) {
        logger().trace(format, arg1, arg2);
    }

    public static void trace(String format, Object... arguments) {
        logger().trace(format, arguments);
    }

    public static void trace(String msg, Throwable t) {
        logger().trace(msg, t);
    }

    public static void debug(String msg) {
        logger().debug(msg);
    }

    public static void debug(String format, Object arg) {
        logger().debug(format, arg);
    }

    public static void debug(String format, Object arg1, Object arg2) {
        logger().debug(format, arg1, arg2);
    }

    public static void debug(String format, Object... arguments) {
        logger().debug(format, arguments);
    }

    public static void debug(String msg, Throwable t) {
        logger().debug(msg, t);
    }

    public static void info(String msg) {
        logger().info(msg);
    }

    public static void info(String format, Object arg) {
        logger().info(format, arg);
    }

    public static void info(String format, Object arg1, Object arg2) {
        logger().info(format, arg1, arg2);
    }

    public static void info(String format, Object... arguments) {
        logger().info(format, arguments);
    }

    public static void info(String msg, Throwable t) {
        logger().info(msg, t);
    }

    public static void warn(String msg) {
        logger().warn(msg);
    }

    public static void warn(String format, Object arg) {
        logger().warn(format, arg);
    }

    public static void warn(String format, Object arg1, Object arg2) {
        logger().warn(format, arg1, arg2);
    }

    public static void warn(String format, Object... arguments) {
        logger().warn(format, arguments);
    }

    public static void warn(String msg, Throwable t) {
        logger().warn(msg, t);
    }

    public static void error(String msg) {
        logger().error(msg);
    }

    public static void error(String format, Object arg) {
        logger().error(format, arg);
    }

    public static void error(String format, Object arg1, Object arg2) {
        logger().error(format, arg1, arg2);
    }

    public static void error(String format, Object... arguments) {
        logger().error(format, arguments);
    }

    public static void error(String msg, Throwable t) {
        logger().error(msg, t);
    }
}
