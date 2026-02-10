package io.github.jiangood.openadmin.framework.middleware.mq.core;

public interface MQListener {



    Result onMessage(Message msg);
}
