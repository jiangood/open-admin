package io.github.jiangood.openadmin.framework.middleware.mq;

import io.github.jiangood.openadmin.framework.middleware.mq.annotation.MQMessageListener;
import io.github.jiangood.openadmin.framework.middleware.mq.core.MQListener;
import io.github.jiangood.openadmin.framework.middleware.mq.core.Message;
import io.github.jiangood.openadmin.framework.middleware.mq.core.MessageQueueTemplate;
import io.github.jiangood.openadmin.framework.middleware.mq.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 消息队列重试测试
 */
@SpringBootTest
public class MQRetryTest {

    @Autowired
    private MessageQueueTemplate messageQueueTemplate;

    @Autowired
    private TestRetryListener testRetryListener;

    @Test
    public void testMessageRetry() throws InterruptedException {
        // 重置计数器
        testRetryListener.resetCount();
        
        // 发送测试消息
        System.out.println("发送测试消息...");
        boolean success = messageQueueTemplate.send("test-retry", "test", "Test message for retry");
        assert success : "消息发送失败";
        
        // 等待测试完成
        System.out.println("等待测试完成...");
        boolean completed = testRetryListener.getLatch().await(30, TimeUnit.SECONDS);
        assert completed : "测试超时";
        
        // 验证重试次数
        System.out.println("重试次数: " + testRetryListener.getCount());
        System.out.println("测试完成");
    }

    /**
     * 测试重试监听器
     * 故意返回RETRY_LATER，触发重试机制
     */
    @Slf4j
    @Component
    @MQMessageListener(topic = "test-retry")
    public static class TestRetryListener implements MQListener {

        private int count = 0;
        private CountDownLatch latch = new CountDownLatch(1);

        @Override
        public Result onMessage(Message msg) {
            count++;
            log.info("消费消息 ({}次): id={}, topic={}, message={}", count, msg.getId(), msg.getTopic(), msg.getBody());
            
            // 前2次返回重试，第3次应该被丢弃
            if (count < 3) {
                log.info("返回RETRY_LATER");
                return Result.RETRY_LATER;
            } else {
                // 第3次调用后，消息应该被丢弃
                log.info("消息处理完成");
                latch.countDown();
                return Result.SUCCESS;
            }
        }

        public int getCount() {
            return count;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        public void resetCount() {
            count = 0;
            latch = new CountDownLatch(1);
        }
    }
}
