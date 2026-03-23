package io.github.jiangood.openadmin.framework.migration;

import io.github.jiangood.openadmin.framework.lifecycle.OpenLifecycle;
import io.github.jiangood.openadmin.framework.lifecycle.OpenLifecycleBeforeJpaInit;
import io.github.jiangood.openadmin.lang.jdbc.DbTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class MigrationSysDict implements OpenLifecycleBeforeJpaInit {

    @Override
    public void process(DbTool db) {
        List<String> tableNames = db.getTableNames();

        if(tableNames.contains("sys_dict")){
            log.warn("清理历史数据字典...");
            db.dropTable("sys_dict_item");
            db.dropTable("sys_dict");
        }
    }
}
