package io.github.jiangood.openadmin.util;

import lombok.Getter;

@Getter
public class BusinessException extends IllegalStateException {

    private final int code;

    public BusinessException() {
        super();
        this.code = 0;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 0;
    }

    public BusinessException(String prefixMessage, Throwable e) {
        super(prefixMessage + ": " + e.getMessage());
        this.code = 0;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
