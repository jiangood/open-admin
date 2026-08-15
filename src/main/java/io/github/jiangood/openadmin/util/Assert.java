package io.github.jiangood.openadmin.util;

public class Assert {
    private Assert() {
    }



    public static void state(boolean state, int code, String msg) {
        if (!state) {
            throw new BusinessException(code, msg);
        }
    }
}
