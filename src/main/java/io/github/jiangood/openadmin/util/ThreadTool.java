package io.github.jiangood.openadmin.util;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ThreadTool implements DisposableBean {

    /**
     * 关闭线程池时等待在途任务的上限（毫秒）
     */
    private static final long CLOSE_WAIT_MS = 5_000;

    private static final class AsyncExecutorHolder {
        static final ExecutorService INSTANCE = new ThreadPoolExecutor(
                4, 16,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(256),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public static void execute(Runnable runnable) {
        AsyncExecutorHolder.INSTANCE.execute(runnable);
    }

    /**
     * 带自增序号的 daemon 线程工厂：{@code prefix-1}、{@code prefix-2} …
     */
    public static ThreadFactory daemon(String prefix) {
        AtomicInteger seq = new AtomicInteger();
        return r -> {
            Thread t = new Thread(r, prefix + "-" + seq.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
    }

    /**
     * 固定名字的 daemon 线程工厂（单线程调度器：一个名字就一个线程，不需要序号）。
     */
    public static ThreadFactory daemonFixed(String name) {
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        };
    }

    /**
     * 单线程调度器，所有线程均为 daemon，不阻止 JVM 退出。
     */
    public static ScheduledExecutorService singleScheduler(String name) {
        return Executors.newSingleThreadScheduledExecutor(daemonFixed(name));
    }

    /**
     * 单线程工作线程，用于把可能阻塞的活从调度线程挪走。
     */
    public static ExecutorService singleWorker(String name) {
        return Executors.newSingleThreadExecutor(daemonFixed(name));
    }

    /**
     * 有界工作池：core = max = {@code threads}，队列容量 {@code queueCapacity}。
     *
     * <p>必须 {@code core = max} 且 {@code allowCoreThreadTimeOut}：{@code core=0} 配上有界队列时
     * {@link ThreadPoolExecutor} 只会排队、不建线程，池子等于不工作。</p>
     *
     * <p>拒绝策略用 {@code AbortPolicy}：调用方靠 {@code RejectedExecutionException}
     * 知道「池满」。</p>
     */
    public static ThreadPoolExecutor boundedPool(String prefix, int threads, int queueCapacity) {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                threads, threads, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity), daemon(prefix),
                new ThreadPoolExecutor.AbortPolicy());
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    /**
     * 关闭线程池：先等在途任务，最多 {@value #CLOSE_WAIT_MS} 毫秒，超时才中断。
     */
    public static void close(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(CLOSE_WAIT_MS, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    /**
     * 立即关闭线程池（中断在途任务）。
     */
    public static void closeNow(ExecutorService executor) {
        executor.shutdownNow();
    }

    /**
     * 睡眠；被中断时恢复中断标志，不要在 catch 里吞掉。
     */
    public static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void destroy() {
        AsyncExecutorHolder.INSTANCE.shutdown();
        try {
            if (!AsyncExecutorHolder.INSTANCE.awaitTermination(10, TimeUnit.SECONDS)) {
                AsyncExecutorHolder.INSTANCE.shutdownNow();
            }
        } catch (InterruptedException e) {
            AsyncExecutorHolder.INSTANCE.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
