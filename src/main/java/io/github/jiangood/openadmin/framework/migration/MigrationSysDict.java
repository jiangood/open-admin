package io.github.jiangood.openadmin.framework.migration;

import io.github.jiangood.openadmin.framework.config.OpenLifecycleBeforeJpaInit;
import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.util.jdbc.DbTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationSysDict implements OpenLifecycleBeforeJpaInit {

    private final SystemProperties systemProperties;

    @Override
    public void process(DbTool db) {
        List<String> tableNames = db.getTableNames();

        if (!tableNames.contains("sys_dict")) {
            return;
        }

        if (systemProperties.isMigrationDropOldTables()) {
            log.warn("清理历史数据字典...");
            db.dropTable("sys_dict_item");
            db.dropTable("sys_dict");
        } else {
            String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            log.warn("备份历史数据字典表至 sys_dict_bak_{} ...", suffix);
            db.renameTable("sys_dict", "sys_dict_bak_" + suffix);
            db.renameTable("sys_dict_item", "sys_dict_item_bak_" + suffix);
        }
    }
}
