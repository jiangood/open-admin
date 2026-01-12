package io.github.jiangood.as.modules.job.dao;

import io.github.jiangood.as.framework.data.repository.BaseDao;
import io.github.jiangood.as.modules.job.entity.SysJob;
import io.github.jiangood.as.modules.job.entity.SysJobExecuteRecord;
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
