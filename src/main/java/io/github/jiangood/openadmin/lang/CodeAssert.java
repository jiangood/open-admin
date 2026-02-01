package io.github.jiangood.openadmin.lang;

public class CodeAssert {


    public static void state(boolean state, int code, String msg) {
        if (!state) {
            throw new CodeException(code, msg);
        }
    }
}
