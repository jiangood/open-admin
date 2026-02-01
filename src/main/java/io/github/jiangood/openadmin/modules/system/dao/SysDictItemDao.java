package io.github.jiangood.openadmin.modules.system.dao;


import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.modules.system.entity.SysDict;
import io.github.jiangood.openadmin.modules.system.entity.SysDictItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统字典值
 */
@Slf4j
@Repository
public class SysDictItemDao extends BaseDao<SysDictItem> {
    @Transactional
    public SysDictItem add(SysDict dict, String code, String text) {
        SysDictItem item = new SysDictItem();
        item.setSysDict(dict);
        item.setCode(code);
        item.setText(text);
        item.setBuiltin(false);

        return this.save(item);
    }

    @Override
    public void updateField(SysDictItem entity, List<String> fieldsToUpdate) {
        super.updateField(entity, fieldsToUpdate);
    }

    @Transactional
    public void deleteByPid(String typeId) {

        List<SysDictItem> list = this.findAllByField(SysDictItem.Fields.sysDict + ".id", typeId);

        this.deleteAll(list);
    }


    public SysDictItem findByDictAndCode(SysDict dict, String code) {
        return this.findByField(SysDictItem.Fields.code, code, SysDictItem.Fields.sysDict, dict);
    }


    @Transactional
    public void saveOrUpdate(SysDict sysDict, String code, String text, int seq, boolean buildin) {
        SysDictItem item = this.findByDictAndCode(sysDict, code);
        if (item == null) {
            item = new SysDictItem();
        }
        item.setCode(code);
        item.setText(text);
        item.setSeq(seq);
        item.setSysDict(sysDict);
        item.setBuiltin(buildin);
        this.save(item);
    }
}
