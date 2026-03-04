package io.github.jiangood.openadmin.lang;

import lombok.Getter;

@Getter
public class BusinessException extends IllegalStateException {

    private int code;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String prefixMessage, Throwable e) {
        super(prefixMessage + ": " + e.getMessage());
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
