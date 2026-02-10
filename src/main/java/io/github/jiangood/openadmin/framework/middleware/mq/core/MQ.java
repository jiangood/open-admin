package io.github.jiangood.openadmin.framework.middleware.mq.core;

import io.github.jiangood.openadmin.framework.middleware.mq.MessageQueueTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * 基于内存的mq实现
 */
@Slf4j
public class MQ implements MessageQueueTemplate {

    // 存储所有topic对应的队列
    private final Map<String, BlockingQueue<Message>> topicQueues = new HashMap<>();
    private final MultiValueMap<String, MQListener> consumers = new LinkedMultiValueMap<>();

    /**
     * 发送消息到指定topic（自动创建队列）
     */
    public synchronized boolean send(String topic, String tag, String message) {
        BlockingQueue<Message> queue = topicQueues.get(topic);
        if (queue == null) throw new IllegalStateException("消息队列未创建");
        try {
            queue.put(new Message(topic, tag, message));

            log.info("消息发送成功: topic={}, message={}", topic, message);
            return true;
        } catch (Exception e) {
            log.error("消息发送失败: topic={}, message={}", topic, message);

            return false;
        }

    }


    /**
     * 订阅指定topic的消息
     */
    public void subscribe(String topic, MQListener listener) {
        consumers.add(topic, listener);
    }

    private ExecutorService executorService;

    public void start() {
        if (consumers.isEmpty()) throw new IllegalArgumentException("启动失败：至少需要一个消费者");
        for (String topic : consumers.keySet()) {
            topicQueues.computeIfAbsent(topic, k -> new LinkedBlockingQueue<>());
        }

        Set<String> topics = topicQueues.keySet();

        executorService = Executors.newFixedThreadPool(topics.size());


        for (String topic : topics) {
            BlockingQueue<Message> queue = topicQueues.get(topic);
            executorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Message message = queue.take();
                        List<MQListener> consumers = this.consumers.get(topic);
                        for (MQListener consumer : consumers) {
                            consumer.consume(message);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        System.err.println("消息消费异常: " + e.getMessage());
                    }
                }
            });
        }
    }


    /**
     * 停止所有消费者
     */
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
            topicQueues.clear();
            consumers.clear();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        MQ queue = new MQ();

        // 订阅topic1
        queue.subscribe("周杰伦", message -> {
            System.out.println("周粉丝1: " + message);
            return Result.SUCCESS;
        });
        queue.subscribe("周杰伦", message -> {
            System.out.println("周粉丝2: " + message);
            return Result.SUCCESS;
        });

        // 订阅topic2
        queue.subscribe("蔡琴", message -> {
            System.out.println("蔡琴粉丝: " + message);
            return Result.SUCCESS;
        });

        queue.start();

        queue.send("周杰伦", "", "Hello World 1");
        queue.send("蔡琴", "", "Test Message");

    }
}