package io.github.jiangood.openadmin.framework.spi;

import java.io.File;
import java.io.InputStream;

public interface FileOperator {

    void save(String key, InputStream inputStream) throws Exception;

    void saveFile(String key, File file) throws Exception;

    InputStream getFileStream(String key) throws Exception;

    void downloadFile(String key, File target) throws Exception;

    void delete(String key) throws Exception;

    boolean exist(String key);

    default Object getClient() {
        throw new UnsupportedOperationException();
    }
}
