package io.github.jiangood.openadmin.modules.log.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class FileLogTool {


    /***
     * 获取日志
     * @param key 存储的日志文件名字， 浏览器可直接访问 admin/sys/log/{key}， 查看文件内容
     * @return
     */
    public static Logger getLogger(String key) {
        MDC.put(FileLogConfig.DISCRIMINATOR_KEY, key);
        return LoggerFactory.getLogger(FileLogConfig.LOGGER_NAME);
    }


    /***
     * 清空MDC, 在线程复用的情况下，需手动清除
     */

    public static void clear() {
        MDC.clear();
    }


}
