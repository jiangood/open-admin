package io.github.jiangood.openadmin.framework.middleware.mq.core;

import cn.hutool.core.util.IdUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Message {
    Long id;
    String topic;
    String tag;
    String message;
    Integer retryCount = 0;
    Boolean isDeadLetter = false;

    public Message(String topic, String tag, String message) {
        this.id = IdUtil.getSnowflakeNextId();
        this.topic = topic;
        this.tag = tag;
        this.message = message;
        this.retryCount = 0;
        this.isDeadLetter = false;
    }
}
