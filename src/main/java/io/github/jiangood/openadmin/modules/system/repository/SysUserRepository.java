package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface SysUserRepository extends BaseRepository<SysUser, String> {

    Optional<SysUser> findByAccount(String account);

    List<SysUser> findAllByEnabledTrueAndIdIn(Collection<String> ids);

    List<SysUser> findAllByEnabledTrue();

    List<SysUser> findAllByRolesContains(SysRole role);

    @EntityGraph(attributePaths = "roles")
    Page<SysUser> findAllWithRoles(Specification<SysUser> spec, Pageable pageable);

}
