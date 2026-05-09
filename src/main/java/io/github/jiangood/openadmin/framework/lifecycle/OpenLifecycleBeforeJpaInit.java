package io.github.jiangood.openadmin.framework.lifecycle;

import io.github.jiangood.openadmin.util.jdbc.DbTool;

/**
 * 在jpa初始化之前执行
 */
public interface OpenLifecycleBeforeJpaInit {

    void process(DbTool db);
}
