package io.github.jiangood.openadmin.framework.middleware.mq;

public interface MessageQueueTemplate {

    boolean send(String topic, String tag, String message);

}
