package io.github.jiangood.openadmin.lang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
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

    @Test
    public void testExecutorServiceSingleton() throws Exception {
        // 测试线程池的单例特性
        // 通过反射获取 asyncExecutor 字段
        java.lang.reflect.Field field = ThreadTool.class.getDeclaredField("asyncExecutor");
        field.setAccessible(true);
        
        // 先清除可能存在的线程池实例
        field.set(null, null);
        
        // 第一次调用 execute 方法，应该创建线程池
        CountDownLatch latch1 = new CountDownLatch(1);
        ThreadTool.execute(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch1.countDown();
            }
        });
        latch1.await(1, TimeUnit.SECONDS);
        
        // 获取第一次创建的线程池
        ExecutorService firstExecutor = (ExecutorService) field.get(null);
        Assertions.assertNotNull(firstExecutor, "第一次调用后应该创建线程池");
        
        // 第二次调用 execute 方法，应该使用同一个线程池
        CountDownLatch latch2 = new CountDownLatch(1);
        ThreadTool.execute(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch2.countDown();
            }
        });
        latch2.await(1, TimeUnit.SECONDS);
        
        // 获取第二次调用后的线程池
        ExecutorService secondExecutor = (ExecutorService) field.get(null);
        Assertions.assertNotNull(secondExecutor, "第二次调用后线程池应该存在");
        Assertions.assertSame(firstExecutor, secondExecutor, "两次调用应该使用同一个线程池实例");
    }

    @Test
    public void testExecutorServiceLazyInitialization() throws Exception {
        // 测试线程池的懒加载特性
        // 通过反射获取 asyncExecutor 字段
        java.lang.reflect.Field field = ThreadTool.class.getDeclaredField("asyncExecutor");
        field.setAccessible(true);
        
        // 清除可能存在的线程池实例
        field.set(null, null);
        
        // 检查初始状态，线程池应该为 null
        ExecutorService executor = (ExecutorService) field.get(null);
        Assertions.assertNull(executor, "初始状态下线程池应该为 null");
        
        // 调用 execute 方法，应该创建线程池
        CountDownLatch latch = new CountDownLatch(1);
        ThreadTool.execute(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await(1, TimeUnit.SECONDS);
        
        // 检查线程池是否被创建
        executor = (ExecutorService) field.get(null);
        Assertions.assertNotNull(executor, "调用 execute 方法后线程池应该被创建");
    }
}
