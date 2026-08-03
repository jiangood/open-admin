package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SysJobService extends BaseService<SysJob> {

    private final QuartzManager quartzService;
    private final SysJobLogRepository sysJobLogRepository;
    private final Scheduler scheduler;


    @Transactional
    public SysJob save(SysJob input, List<String> requestKeys) throws Exception {
        SysJob db;
        if (input.isNew()) {
            db = repository.save(input);
        } else {
            this.updateField(input, requestKeys);
            db = repository.findById(input.getId()).orElse(null);
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
        SysJob job = repository.findById(id).orElse(null);
        Assert.notNull(job, "该任务已被删除，请勿重复操作");
        quartzService.deleteJob(job);

        sysJobLogRepository.deleteBySysJobId(id);

        deleteById(id);
    }


    public Page<SysJobLog> findAllExecuteRecord(Specification<SysJobLog> q, Pageable pageable) {
        return sysJobLogRepository.findAll(q, pageable);
    }

    public Page<SysJob> page(String name, String jobClass, Pageable pageable) throws SchedulerException {
        return repository.findAll(Spec.<SysJob>of().like(SysJob.Fields.name, name).like(SysJob.Fields.jobClass, jobClass), pageable);
    }
}
