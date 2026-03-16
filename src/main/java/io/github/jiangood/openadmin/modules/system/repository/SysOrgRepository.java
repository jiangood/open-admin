package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysOrgRepository extends BaseRepository<SysOrg, String> {

    SysOrg findByThirdId(String thirdId);

    List<SysOrg> findByPid(String pid, Sort sort);

    List<SysOrg> findByPidIsNull(Sort sort);

}
