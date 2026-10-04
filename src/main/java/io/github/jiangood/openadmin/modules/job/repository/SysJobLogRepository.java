package io.github.jiangood.openadmin.modules.job.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import org.springframework.stereotype.Repository;

@Repository
public interface SysJobLogRepository extends BaseRepository<SysJobLog, String> {

    void deleteBySysJobId(String jobId);


}
