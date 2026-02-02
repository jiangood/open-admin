package io.github.jiangood.openadmin.lang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTool {

    private static volatile ExecutorService asyncExecutor;
    public static void execute(Runnable runnable) {
        if(asyncExecutor == null){
            asyncExecutor = Executors.newCachedThreadPool();
        }
        asyncExecutor.execute(runnable);
    }
}
