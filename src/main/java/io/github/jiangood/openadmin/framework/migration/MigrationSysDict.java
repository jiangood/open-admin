package io.github.jiangood.openadmin.framework.migration;

import cn.hutool.core.util.ArrayUtil;
import io.github.jiangood.openadmin.framework.config.init.OpenLifecycle;
import io.github.jiangood.openadmin.lang.jdbc.DbTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationSysDict implements OpenLifecycle {

    private final DbTool db;

    @Override
    public void beforeJpaInit() {
        Set<String> tableNames = db.getTableNames();

        if(tableNames.contains("sys_dict")){
            log.warn("清理历史数据字典...");
            db.dropTable("sys_dict_item");
            db.dropTable("sys_dict");
        }
    }

}
