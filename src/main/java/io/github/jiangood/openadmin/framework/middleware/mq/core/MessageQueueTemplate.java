package io.github.jiangood.openadmin.framework.middleware.mq.core;

public interface MessageQueueTemplate {

    boolean send(String topic, String tag, String message);

}
