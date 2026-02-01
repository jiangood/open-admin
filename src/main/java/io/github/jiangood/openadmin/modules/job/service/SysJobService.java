package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.job.dao.SysJobExecuteRecordDao;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobExecuteRecord;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
public class SysJobService extends BaseService<SysJob> {

    @Resource
    QuartzManager quartzService;

    @Resource
    SysJobExecuteRecordDao sysJobExecuteRecordDao;

    @Resource
    Scheduler scheduler;


    @Override
    public SysJob saveOrUpdateByUserAction(SysJob input, List<String> updateKeys) throws Exception {
        SysJob db = super.saveOrUpdateByUserAction(input, updateKeys);

        quartzService.deleteJob(db);
        if (db.getEnabled()) {
            quartzService.scheduleJob(db);
        }

        return null;
    }


    @Transactional
    public void deleteJob(String id) throws SchedulerException {
        log.info("删除定时任务 {}", id);
        SysJob job = baseDao.findOne(id);
        Assert.notNull(job, "该任务已被删除，请勿重复操作");
        quartzService.deleteJob(job);

        sysJobExecuteRecordDao.deleteByJobId(id);

        baseDao.deleteById(id);
    }


    public Page<SysJobExecuteRecord> findAllExecuteRecord(Specification<SysJobExecuteRecord> q, Pageable pageable) {
        return sysJobExecuteRecordDao.findAll(q, pageable);
    }

    public Page<SysJob> page(String searchText, Pageable pageable) throws SchedulerException {
        return baseDao.findAll(Spec.<SysJob>of().orLike(searchText, SysJob.Fields.name, SysJob.Fields.jobClass), pageable);
    }
}
