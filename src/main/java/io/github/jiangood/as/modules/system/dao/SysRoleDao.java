package io.github.jiangood.as.modules.system.dao;


import io.github.jiangood.as.framework.data.repository.BaseDao;
import io.github.jiangood.as.modules.system.entity.SysRole;
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
