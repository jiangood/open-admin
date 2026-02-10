package io.github.jiangood.openadmin.framework.middleware.mq;

import cn.hutool.core.lang.Assert;
import io.github.jiangood.openadmin.framework.middleware.mq.annotation.MQMessageListener;
import io.github.jiangood.openadmin.framework.middleware.mq.core.MQ;
import io.github.jiangood.openadmin.framework.middleware.mq.core.MQListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(prefix = "sys.simple-mq", name = "enabled")
@Configuration
public class MqConfig implements SmartLifecycle {

    private MQ mq;

    private final List<MQListener> listeners;

    @Bean
    public MessageQueueTemplate messageQueueTemplate(){
        return mq;
    }

    @Override
    public void start() {
        if (listeners == null || listeners.isEmpty()) {
            return;
        }

        log.info("简单MQ启动...");
        try {
            mq = new MQ();
            for (MQListener listener : listeners) {
                MQMessageListener annotation = listener.getClass().getAnnotation(MQMessageListener.class);
                Assert.notNull(annotation, listener.getClass().getSimpleName() + " does not have annotation " + MQMessageListener.class.getSimpleName());
                mq.subscribe(annotation.topic(), listener);
            }

            mq.start();
            log.info("简单MQ成功...");
        } catch (Exception e) {
            log.error("简单MQ启动失败", e);
        }

    }

    @Override
    public void stop() {
        log.info("简单MQ停止");
        if (this.isRunning()) {
            mq.shutdown();
            mq = null;
        }
    }

    @Override
    public boolean isRunning() {
        return mq != null;
    }
}
