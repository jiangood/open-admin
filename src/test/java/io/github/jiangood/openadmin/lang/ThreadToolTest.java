package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThreadToolTest {

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
                e.printStackTrace();
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
        try {
            ThreadTool.execute(null);
            // 应该抛出NullPointerException
            Assertions.fail("应该抛出NullPointerException");
        } catch (NullPointerException e) {
            // 预期会抛出异常
        }
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
                    e.printStackTrace();
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
}
