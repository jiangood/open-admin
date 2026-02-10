package io.github.jiangood.openadmin.framework.middleware.mq.core;

import lombok.Getter;

@Getter
public enum Result {

    /**
     * 消费成功
     */
    SUCCESS(true),

    /**
     * 消费失败，需要重试
     */
    RETRY_LATER(false),

    /**
     * 消费失败，无需重试（进入死信队列）
     */
    FAILURE(false),

    /**
     * 重复消息，直接丢弃
     */
    DUPLICATE(true),

    /**
     * 消息已进入死信队列
     */
    DEAD_LETTER(false);

    private final boolean success;

    Result(boolean success) {
        this.success = success;
    }

}
