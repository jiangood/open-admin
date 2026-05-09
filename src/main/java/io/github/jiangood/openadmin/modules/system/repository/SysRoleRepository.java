package io.github.jiangood.openadmin.modules.system.repository;


import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 系统角色
 */
@Repository
public interface SysRoleRepository extends BaseRepository<SysRole, String> {

    Optional<SysRole> findByCode(String code);

    long countByCode(String code);

    List<SysRole> findAllByEnabled(boolean enabled);

    List<SysRole> findAllByCodeIn(Collection<String> code);

}
