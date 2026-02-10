package io.github.jiangood.openadmin.framework.middleware.mq;

import org.springframework.stereotype.Component;
import io.github.jiangood.openadmin.framework.middleware.mq.annotation.MQMessageListener;
import io.github.jiangood.openadmin.framework.middleware.mq.core.MQListener;
import io.github.jiangood.openadmin.framework.middleware.mq.core.Message;
import io.github.jiangood.openadmin.framework.middleware.mq.core.Result;

@Component
@MQMessageListener(topic = "demo")
public class DemoMqListener implements MQListener {


    @Override
    public Result onMessage(Message msg) {
        System.out.println("消费消息" + msg);
        return Result.SUCCESS;
    }
}
