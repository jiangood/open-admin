package io.github.jiangood.as.framework;

public class CodeException extends IllegalStateException {

    private int code;

    public CodeException() {
        super();
    }

    public CodeException(String message) {
        super(message);
    }

    public CodeException(String prefixMessage, Throwable e) {
        super(prefixMessage + ": " + e.getMessage());
    }

    public CodeException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
