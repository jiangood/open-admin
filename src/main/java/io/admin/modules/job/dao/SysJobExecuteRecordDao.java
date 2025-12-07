package io.admin.modules.job.dao;

import io.admin.framework.data.repository.BaseDao;
import io.admin.modules.job.entity.SysJob;
import io.admin.modules.job.entity.SysJobExecuteRecord;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class SysJobExecuteRecordDao extends BaseDao<SysJobExecuteRecord> {


    @Transactional
    public void deleteByJobId(String jobId) {
        List<SysJobExecuteRecord> list = this.findAllByField(SysJobExecuteRecord.Fields.sysJob, new SysJob(jobId));
        this.deleteAll(list);
    }


}
