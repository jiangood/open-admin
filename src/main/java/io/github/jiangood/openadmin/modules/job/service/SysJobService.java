package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.job.dao.SysJobDao;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
public class SysJobService {

    @Resource
    private SysJobDao sysJobDao;

    @Resource
    QuartzManager quartzService;

    @Resource
    SysJobExecuteRecordDao sysJobExecuteRecordDao;

    @Resource
    Scheduler scheduler;


    @Transactional
    public SysJob save(SysJob input, List<String> requestKeys) throws Exception {
        SysJob db;
        if (input.isNew()) {
            db = sysJobDao.save(input);
        } else {
            sysJobDao.updateField(input, requestKeys);
            db = sysJobDao.findById(input.getId()).orElse(null);
        }

        quartzService.deleteJob(db);
        if (db.getEnabled()) {
            quartzService.scheduleJob(db);
        }

        return null;
    }


    @Transactional
    public void deleteJob(String id) throws SchedulerException {
        log.info("删除定时任务 {}", id);
        SysJob job = sysJobDao.findById(id).orElse(null);
        Assert.notNull(job, "该任务已被删除，请勿重复操作");
        quartzService.deleteJob(job);

        sysJobExecuteRecordDao.deleteBySysJobId(id);

        sysJobDao.deleteById(id);
    }


    public Page<SysJobExecuteRecord> findAllExecuteRecord(Specification<SysJobExecuteRecord> q, Pageable pageable) {
        return sysJobExecuteRecordDao.findAll(q, pageable);
    }

    public Page<SysJob> page(String searchText, Pageable pageable) throws SchedulerException {
        return sysJobDao.findAll(Spec.<SysJob>of().orLike(searchText, SysJob.Fields.name, SysJob.Fields.jobClass), pageable);
    }

    // BaseService 方法
    public Page<SysJob> getPage(Specification<SysJob> spec, Pageable pageable) {
        return sysJobDao.findAll(spec, pageable);
    }

    public SysJob detail(String id) {
        return sysJobDao.findById(id).orElse(null);
    }

    public SysJob get(String id) {
        return sysJobDao.findById(id).orElse(null);
    }

    public List<SysJob> getAll() {
        return sysJobDao.findAll();
    }

    public List<SysJob> getAll(Sort sort) {
        return sysJobDao.findAll(sort);
    }

    public List<SysJob> getAll(Specification<SysJob> s, Sort sort) {
        return sysJobDao.findAll(s, sort);
    }

    public Spec<SysJob> spec() {
        return Spec.of();
    }

    public SysJob save(SysJob t) {
        return sysJobDao.save(t);
    }

    @Transactional
    public void delete(String id) {
        sysJobDao.deleteById(id);
    }
}
