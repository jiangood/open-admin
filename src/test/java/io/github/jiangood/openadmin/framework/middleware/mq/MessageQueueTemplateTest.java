package io.github.jiangood.openadmin.framework.middleware.mq;

import io.github.jiangood.openadmin.framework.middleware.mq.core.MessageQueueTemplate;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MessageQueueTemplateTest {

    @Resource
    private  MessageQueueTemplate messageQueueTemplate;
    @Test
    void send() throws InterruptedException {

        Thread.sleep(5000);
        messageQueueTemplate.send("demo", "", "hello world");

        Thread.sleep(5000);
    }
}