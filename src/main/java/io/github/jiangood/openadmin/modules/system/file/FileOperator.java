package io.github.jiangood.openadmin.modules.system.file;


import java.io.File;
import java.io.InputStream;

/**
 * 文件操纵者（内网操作）
 * 如果存在未包含的操作，可以调用getClient()自行获取client进行操作
 */
public interface FileOperator {


    /**
     * 文件管理 ,流式
     *
     * @param key         唯一标示id，例如a.txt, doc/a.txt
     * @param inputStream 文件流
     */
    void save(String key, InputStream inputStream) throws Exception;

    void saveFile(String key, File file) throws Exception;

    InputStream getFileStream(String key) throws Exception;

    void downloadFile(String key, File target) throws Exception;

    /**
     * 删除文件
     *
     * @param key 文件唯一标识
     */
    void delete(String key) throws Exception;


    boolean exist(String key);

}
