package io.github.jiangood.openadmin.modules.job.dao;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysJobDao extends BaseRepository<SysJob, String> {

    List<SysJob> findAllByEnabledTrue();

    SysJob findByName(String name);

    SysJob findByNameAndGroup(String name);

}
