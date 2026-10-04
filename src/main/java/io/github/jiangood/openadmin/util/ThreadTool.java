package io.github.jiangood.openadmin.util;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ThreadTool implements DisposableBean {

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
