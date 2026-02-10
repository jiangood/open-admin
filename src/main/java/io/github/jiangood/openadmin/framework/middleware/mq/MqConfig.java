package io.github.jiangood.openadmin.framework.middleware.mq;

import cn.hutool.core.lang.Assert;
import io.github.jiangood.openadmin.framework.middleware.mq.annotation.MQMessageListener;
import io.github.jiangood.openadmin.framework.middleware.mq.core.DbRep;
import io.github.jiangood.openadmin.framework.middleware.mq.core.MQ;
import io.github.jiangood.openadmin.framework.middleware.mq.core.MQListener;
import io.github.jiangood.openadmin.framework.middleware.mq.core.MessageQueueTemplate;
import io.github.jiangood.openadmin.lang.jdbc.DbTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnBooleanProperty(prefix = "sys", name = "simple-message-queue-enabled", matchIfMissing = true)
@Configuration
public class MqConfig implements SmartLifecycle {

    private MQ mq ;

    private final List<MQListener> listeners;

    public MqConfig(List<MQListener> listeners, DbTool dbTool) {
        this.listeners = listeners;
        this.mq = new MQ(new DbRep(dbTool));
        this.init();
    }

    @Bean
    public MessageQueueTemplate messageQueueTemplate() {
        return mq;
    }

    @Override
    public void start() {
        init();
    }

    private void init() {
        log.info("简单MQ启动...");
        try {
            for (MQListener listener : listeners) {
                MQMessageListener annotation = listener.getClass().getAnnotation(MQMessageListener.class);
                if (annotation != null) {
                    mq.subscribe(annotation.topic(), listener);
                    log.info("注册队列处理器: topic={}, listener={}", annotation.topic(), listener.getClass().getSimpleName());
                }
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
        return mq != null && mq.isRunning();
    }
}
