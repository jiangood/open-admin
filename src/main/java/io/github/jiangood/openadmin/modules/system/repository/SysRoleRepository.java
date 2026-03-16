package io.github.jiangood.openadmin.modules.system.repository;


import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 系统角色
 */
@Repository
public interface SysRoleRepository extends BaseRepository<SysRole, String> {

    SysRole findByCode(String code);

    long countByCode(String code);

    List<SysRole> findAllByEnabled(boolean enabled);

    List<SysRole> findAllByCodeIn(Collection<String> code);

}
