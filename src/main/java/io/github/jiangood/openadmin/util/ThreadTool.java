package io.github.jiangood.openadmin.util;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ThreadTool implements DisposableBean {

    private static volatile ExecutorService asyncExecutor;

    public static void execute(Runnable runnable) {
        if (asyncExecutor == null) {
            synchronized (ThreadTool.class) {
                if (asyncExecutor == null) {
                    asyncExecutor = new ThreadPoolExecutor(
                            4, 16,
                            60L, TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(256),
                            new ThreadPoolExecutor.AbortPolicy()
                    );
                }
            }
        }
        asyncExecutor.execute(runnable);
    }

    @Override
    public void destroy() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
