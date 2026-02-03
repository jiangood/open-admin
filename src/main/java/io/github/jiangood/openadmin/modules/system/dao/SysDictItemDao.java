package io.github.jiangood.openadmin.modules.system.dao;


import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统字典值
 */
@Slf4j
@Repository
public class SysDictItemDao extends BaseDao<SysDictItem> {


    @Override
    public void updateField(SysDictItem entity, List<String> fieldsToUpdate) {
        super.updateField(entity, fieldsToUpdate);
    }


}
