package io.github.jiangood.openadmin.framework.middleware.mq.core;

import cn.hutool.core.util.IdUtil;
import lombok.Data;

@Data
public class Message {
    Long id;
    String topic;
    String tag;
    String message;

    public Message(String topic, String tag, String message) {
        this.id = IdUtil.getSnowflakeNextId();
        this.topic = topic;
        this.tag = tag;
        this.message = message;
    }
}
