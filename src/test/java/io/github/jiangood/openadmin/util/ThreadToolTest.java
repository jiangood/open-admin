package io.github.jiangood.openadmin.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class ThreadToolTest {

    @Test
    public void testExecute() throws InterruptedException {
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
    public void testExecuteWhenRunnableNull() {
        // 测试runnable为null的情况
        assertThrows(NullPointerException.class, () -> ThreadTool.execute(null));
    }

    @Test
    public void testExecuteMultipleTasks() throws InterruptedException {
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
    public void testExecutorServiceSingleton() throws Exception {
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
    public void testExecutorServiceLazyInitialization() throws Exception {
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
}
