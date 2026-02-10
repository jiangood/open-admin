package io.github.jiangood.openadmin.framework.middleware.mq.core;

import io.github.jiangood.openadmin.framework.middleware.mq.annotation.MQMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 死信队列处理器
 * 处理超过最大重试次数的消息
 */
@Slf4j
@Component
@MQMessageListener(topic = "dead-letter")
public class DeadLetterQueueListener implements MQListener {

    @Override
    public Result consume(Message msg) {
        try {
            // 处理死信消息
            log.warn("处理死信消息: id={}, topic={}, tag={}, retryCount={}, message={}", 
                    msg.getId(), msg.getTopic(), msg.getTag(), msg.getRetryCount(), msg.getMessage());
            
            // 这里可以添加具体的死信处理逻辑，例如：
            // 1. 记录到日志文件
            // 2. 发送告警通知
            // 3. 存储到专门的死信表
            // 4. 人工处理
            
            // 模拟处理时间
            Thread.sleep(100);
            
            log.info("死信消息处理完成: id={}", msg.getId());
            return Result.SUCCESS;
        } catch (Exception e) {
            log.error("死信消息处理失败: id={}", msg.getId(), e);
            return Result.FAILURE;
        }
    }
}
