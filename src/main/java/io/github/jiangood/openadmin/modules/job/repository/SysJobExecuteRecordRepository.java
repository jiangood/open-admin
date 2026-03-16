package io.github.jiangood.openadmin.modules.job.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.job.entity.SysJobExecuteRecord;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SysJobExecuteRecordRepository extends BaseRepository<SysJobExecuteRecord, String> {

    void deleteBySysJobId(String jobId);


}
