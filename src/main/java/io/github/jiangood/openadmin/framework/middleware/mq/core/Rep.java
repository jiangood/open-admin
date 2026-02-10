package io.github.jiangood.openadmin.framework.middleware.mq.core;

import java.util.List;

public interface Rep {

    void save(Message msg);

    void delete(Long id);

    List<Message> loadAll();
}
