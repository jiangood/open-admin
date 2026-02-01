package io.github.jiangood.openadmin.modules.system.dao;


import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import org.springframework.stereotype.Repository;

/**
 * 系统角色
 */
@Repository
public class SysRoleDao extends BaseDao<SysRole> {

    public SysRole findByCode(String code) {
        return this.findByField(SysRole.Fields.code, code);
    }

    public long countByCode(String code) {
        return this.count(spec().eq(SysRole.Fields.code, code));
    }
}
