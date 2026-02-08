package io.github.jiangood.openadmin.framework.migration;

import cn.hutool.core.util.ArrayUtil;
import io.github.jiangood.openadmin.framework.config.init.SystemHook;
import io.github.jiangood.openadmin.lang.ArrayTool;
import io.github.jiangood.openadmin.lang.jdbc.DbTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MigrationSysDict implements SystemHook {

    private final DbTool db;


    @Override
    public void beforeDataInit() {
        Set<String> tableNames = db.getTableNames();
        if(!tableNames.contains("sys_dict_item")){
            return;
        }

        Long count = db.findLong("SELECT count(type_code) from sys_dict_item");
        if(count == 0){
            // 删除外键
            db.executeQuietly("ALTER TABLE `sys_dict_item` DROP FOREIGN KEY `FK4wt3klc6clhr4266y25g07dg4`");

            if(tableNames.contains("sys_dict")){
                // 填充typeCode
                Map<String, Object> map = db.findTwoFieldListToMap("SELECT t.id, d.`code` from sys_dict_item t LEFT JOIN sys_dict d on d.id = t.sys_dict_id");
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    db.update("UPDATE sys_dict_item SET type_code = ? WHERE id = ?", entry.getValue(), entry.getKey());
                }
            }



            // 删除错误的数据
            db.executeQuietly("DELETE FROM sys_dict_item where type_code in('orgType','materialType','approveStatus','sex','dataPermType','yesNo')");

            db.executeQuietly("ALTER TABLE `sys_dict_item` DROP COLUMN `sys_dict_id`");
            db.executeQuietly("ALTER TABLE `sys_dict_item` DROP COLUMN `builtin`");
        }

        String[] keys = db.getKeys("select * from sys_dict_item");
        if(ArrayUtil.contains(keys,"text")){
            db.executeQuietly("UPDATE sys_dict_item SET name = text;");
            db.executeQuietly("ALTER TABLE `sys_dict_item` DROP COLUMN `text`");
        }

    }
}
