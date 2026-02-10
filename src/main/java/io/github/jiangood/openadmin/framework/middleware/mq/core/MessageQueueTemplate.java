package io.github.jiangood.openadmin.framework.middleware.mq.core;

public class MessageQueueTemplate {

    private final MQ mq;

    public MessageQueueTemplate(MQ mq) {
        this.mq = mq;
    }

    public boolean send(String topic, String tag, String message){
        if (mq == null || !mq.isRunning()) {
            throw new IllegalStateException("MQ未启动");
        }
        return mq.send(topic, tag, message);
    }

}
