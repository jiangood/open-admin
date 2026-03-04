package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface SysUserRepository extends BaseRepository<SysUser, String> {

    SysUser findByAccount(String account);

    List<SysUser> findAllByEnabledTrueAndIdIn(Collection<String> ids);

    List<SysUser> findAllByEnabledTrue();

    List<SysUser> findAllByRolesContains(SysRole role);

}
