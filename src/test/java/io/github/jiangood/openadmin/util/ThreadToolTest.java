package io.github.jiangood.openadmin.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class ThreadToolTest {

    @Test
    void testExecute() throws InterruptedException {
        // 使用CountDownLatch来等待异步任务执行完成
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] executed = {false};

        // 测试执行异步任务
        ThreadTool.execute(() -> {
            try {
                // 模拟任务执行时间
                Thread.sleep(100);
                executed[0] = true;
            } catch (InterruptedException e) {
                log.error("线程中断", e);
            } finally {
                latch.countDown();
            }
        });

        // 等待任务执行完成，最多等待1秒
        boolean await = latch.await(1, TimeUnit.SECONDS);
        Assertions.assertTrue(await, "任务应该在1秒内执行完成");
        Assertions.assertTrue(executed[0], "任务应该被执行");
    }

    @Test
    void testExecuteWhenRunnableNull() {
        // 测试runnable为null的情况
        assertThrows(NullPointerException.class, () -> ThreadTool.execute(null));
    }

    @Test
    void testExecuteMultipleTasks() throws InterruptedException {
        // 测试执行多个异步任务
        int taskCount = 5;
        CountDownLatch latch = new CountDownLatch(taskCount);
        final int[] executedCount = {0};

        for (int i = 0; i < taskCount; i++) {
            ThreadTool.execute(() -> {
                try {
                    // 模拟任务执行时间
                    Thread.sleep(50);
                    synchronized (executedCount) {
                        executedCount[0]++;
                    }
                } catch (InterruptedException e) {
                    log.error("线程中断", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务执行完成，最多等待2秒
        boolean await = latch.await(2, TimeUnit.SECONDS);
        Assertions.assertTrue(await, "所有任务应该在2秒内执行完成");
        Assertions.assertEquals(taskCount, executedCount[0], "所有任务都应该被执行");
    }

    @Test
    void testExecutorServiceSingleton() throws Exception {
        // 测试线程池的单例特性：静态持有者模式保证全局唯一
        Class<?> holderClass = Class.forName("io.github.jiangood.openadmin.util.ThreadTool$AsyncExecutorHolder");
        java.lang.reflect.Field instanceField = holderClass.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);

        ExecutorService firstExecutor = (ExecutorService) instanceField.get(null);
        ExecutorService secondExecutor = (ExecutorService) instanceField.get(null);
        Assertions.assertNotNull(firstExecutor, "线程池实例应存在");
        Assertions.assertSame(firstExecutor, secondExecutor, "两次获取应该使用同一个线程池实例");
    }

    @Test
    void testExecutorServiceLazyInitialization() throws Exception {
        // 静态持有者模式：仅首次访问时初始化，此处验证任务能正常异步执行
        CountDownLatch latch = new CountDownLatch(1);
        ThreadTool.execute(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                log.error("线程中断", e);
            } finally {
                latch.countDown();
            }
        });
        Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS), "异步任务应执行完成");
    }

    @Test
    void testDaemonThreadFactory() {
        ThreadFactory factory = ThreadTool.daemon("unit-daemon");
        Thread first = factory.newThread(() -> {
        });
        Thread second = factory.newThread(() -> {
        });
        Assertions.assertEquals("unit-daemon-1", first.getName());
        Assertions.assertEquals("unit-daemon-2", second.getName());
        Assertions.assertTrue(first.isDaemon());
        Assertions.assertTrue(second.isDaemon());

        Thread fixed = ThreadTool.daemonFixed("unit-fixed").newThread(() -> {
        });
        Assertions.assertEquals("unit-fixed", fixed.getName());
        Assertions.assertTrue(fixed.isDaemon());
    }

    @Test
    void testSingleSchedulerAndWorkerNames() throws InterruptedException {
        ScheduledExecutorService scheduler = ThreadTool.singleScheduler("unit-scheduler");
        ExecutorService worker = ThreadTool.singleWorker("unit-worker");
        try {
            Assertions.assertEquals("unit-scheduler", threadName(scheduler));
            Assertions.assertEquals("unit-worker", threadName(worker));
        } finally {
            ThreadTool.close(scheduler);
            ThreadTool.close(worker);
        }
    }

    @Test
    void testBoundedPoolRejectsWhenFull() throws InterruptedException {
        ThreadPoolExecutor pool = ThreadTool.boundedPool("unit-pool", 1, 1);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            Assertions.assertEquals(1, pool.getCorePoolSize());
            Assertions.assertEquals(1, pool.getMaximumPoolSize());
            Assertions.assertTrue(pool.allowsCoreThreadTimeOut());

            pool.execute(() -> {
                started.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            Assertions.assertTrue(started.await(1, TimeUnit.SECONDS));

            pool.execute(() -> {
            });
            assertThrows(RejectedExecutionException.class, () -> pool.execute(() -> {
            }));
        } finally {
            release.countDown();
            ThreadTool.close(pool);
        }
        Assertions.assertTrue(pool.isShutdown());
    }

    @Test
    void testCloseNowInterruptsRunningTask() throws InterruptedException {
        ExecutorService worker = ThreadTool.singleWorker("unit-close-now");
        CountDownLatch started = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean(false);
        worker.execute(() -> {
            started.countDown();
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        Assertions.assertTrue(started.await(1, TimeUnit.SECONDS));
        ThreadTool.closeNow(worker);
        Assertions.assertTrue(worker.awaitTermination(1, TimeUnit.SECONDS));
        Assertions.assertTrue(interrupted.get());
    }

    @Test
    void testSleepQuietlyRestoresInterruptFlag() {
        Thread.currentThread().interrupt();
        ThreadTool.sleepQuietly(10);
        Assertions.assertTrue(Thread.interrupted(), "中断标志应被保留");
    }

    private static String threadName(ExecutorService executor) throws InterruptedException {
        AtomicReference<String> name = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        executor.execute(() -> {
            name.set(Thread.currentThread().getName());
            latch.countDown();
        });
        Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS), "任务应执行完成");
        return name.get();
    }
}
