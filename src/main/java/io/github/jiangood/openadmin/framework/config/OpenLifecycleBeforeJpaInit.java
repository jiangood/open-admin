package io.github.jiangood.openadmin.framework.config;

import io.github.jiangood.openadmin.util.jdbc.DbTool;

public interface OpenLifecycleBeforeJpaInit {

    void process(DbTool db);
}
