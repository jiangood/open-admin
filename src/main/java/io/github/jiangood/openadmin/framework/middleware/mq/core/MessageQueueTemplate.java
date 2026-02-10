package io.github.jiangood.openadmin.framework.middleware.mq.core;

public class MessageQueueTemplate {

    private final MQ mq;

    public MessageQueueTemplate(MQ mq) {
        this.mq = mq;
    }

    boolean send(String topic, String tag, String message){
        return mq.send(topic, tag, message);
    }

}
