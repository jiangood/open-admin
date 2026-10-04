package io.github.jiangood.openadmin.framework.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FileOperator {

    void save(String key, InputStream inputStream) throws IOException;

    void saveFile(String key, File file) throws IOException;

    InputStream getFileStream(String key) throws IOException;

    void downloadFile(String key, File target) throws IOException;

    void delete(String key) throws IOException;

    boolean exist(String key);

    default Object getClient() {
        throw new UnsupportedOperationException();
    }
}
