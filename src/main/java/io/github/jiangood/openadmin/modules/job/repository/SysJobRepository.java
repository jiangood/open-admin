package io.github.jiangood.openadmin.modules.job.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysJobRepository extends BaseRepository<SysJob, String> {

    List<SysJob> findAllByEnabledTrue();

    SysJob findByName(String name);

}
