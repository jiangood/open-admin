package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobExecuteRecord;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
import io.github.jiangood.openadmin.modules.job.repository.SysJobExecuteRecordRepository;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
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
    private SysJobRepository sysJobRepository;

    @Resource
    QuartzManager quartzService;

    @Resource
    SysJobExecuteRecordRepository sysJobExecuteRecordRepository;

    @Resource
    Scheduler scheduler;


    @Transactional
    public SysJob save(SysJob input, List<String> requestKeys) throws Exception {
        SysJob db;
        if (input.isNew()) {
            db = sysJobRepository.save(input);
        } else {
            sysJobRepository.updateField(input, requestKeys);
            db = sysJobRepository.findById(input.getId()).orElse(null);
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
        SysJob job = sysJobRepository.findById(id).orElse(null);
        Assert.notNull(job, "该任务已被删除，请勿重复操作");
        quartzService.deleteJob(job);

        sysJobExecuteRecordRepository.deleteBySysJobId(id);

        sysJobRepository.deleteById(id);
    }


    public Page<SysJobExecuteRecord> findAllExecuteRecord(Specification<SysJobExecuteRecord> q, Pageable pageable) {
        return sysJobExecuteRecordRepository.findAll(q, pageable);
    }

    public Page<SysJob> page(String searchText, Pageable pageable) throws SchedulerException {
        return sysJobRepository.findAll(Spec.<SysJob>of().orLike(searchText, SysJob.Fields.name, SysJob.Fields.jobClass), pageable);
    }

    // BaseService 方法
    public Page<SysJob> getPage(Specification<SysJob> spec, Pageable pageable) {
        return sysJobRepository.findAll(spec, pageable);
    }

    public SysJob detail(String id) {
        return sysJobRepository.findById(id).orElse(null);
    }

    public SysJob get(String id) {
        return sysJobRepository.findById(id).orElse(null);
    }

    public List<SysJob> getAll() {
        return sysJobRepository.findAll();
    }

    public List<SysJob> getAll(Sort sort) {
        return sysJobRepository.findAll(sort);
    }

    public List<SysJob> getAll(Specification<SysJob> s, Sort sort) {
        return sysJobRepository.findAll(s, sort);
    }

    public Spec<SysJob> spec() {
        return Spec.of();
    }

    public SysJob save(SysJob t) {
        return sysJobRepository.save(t);
    }

    @Transactional
    public void delete(String id) {
        sysJobRepository.deleteById(id);
    }
}
