package io.github.jiangood.openadmin.framework.middleware.mq.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

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
    private final Map<String, MQListener> consumers = new HashMap<>();

    private Rep rep;

    @Getter
    private boolean isRunning = false;

    public MQ(Rep rep) {
        this.rep = rep;
    }

    public MQ() {
    }

    /**
     * 发送消息到指定topic（自动创建队列）
     */
    public synchronized boolean send(String topic, String tag, String message) {
        Assert.state(isRunning, "MQ未启动");
        BlockingQueue<Message> queue = topicQueues.computeIfAbsent(topic, k -> new LinkedBlockingQueue<>());
        try {
            Message e = new Message(topic, tag, message);
            if(rep!= null){
                rep.save(e);
            }
            queue.put(e);

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
        if (consumers.containsKey(topic)) {
            throw new IllegalArgumentException("消费者已存在" + topic);
        }
        consumers.put(topic, listener);
    }

    private ExecutorService executorService;

    public void start() {
        if (consumers.isEmpty()) throw new IllegalArgumentException("启动失败：至少需要一个消费者");
        for (String topic : consumers.keySet()) {
            topicQueues.computeIfAbsent(topic, k -> new LinkedBlockingQueue<>());
        }
        if(rep != null){
            List<Message> dbList = rep.loadAll();
            for (Message message : dbList) {
                BlockingQueue<Message> queue = topicQueues.get(message.getTopic());
                if(queue == null){
                    log.error("消息队列不存在: {}", message.getTopic());
                }else {
                    queue.add(message);
                }
            }
        }


        Set<String> topics = topicQueues.keySet();

        executorService = Executors.newFixedThreadPool(topics.size());


        for (String topic : topics) {
            BlockingQueue<Message> queue = topicQueues.get(topic);
            executorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Message message = queue.take();
                        MQListener consumer = this.consumers.get(topic);
                        consumer.consume(message);
                        if(rep != null){
                            rep.delete(message.getId());
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
        isRunning = true;
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
        isRunning = false;
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