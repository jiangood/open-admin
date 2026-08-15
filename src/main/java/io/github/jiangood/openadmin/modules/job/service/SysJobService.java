package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
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

    private final QuartzManager quartzService;
    private final SysJobLogRepository sysJobLogRepository;

    public SysJobService(SysJobRepository repository, EntityManager entityManager,
                         QuartzManager quartzService, SysJobLogRepository sysJobLogRepository) {
        super(repository, entityManager);
        this.quartzService = quartzService;
        this.sysJobLogRepository = sysJobLogRepository;
    }


    @Transactional(rollbackFor = Exception.class)
    public SysJob save(SysJob input, List<String> requestKeys) throws Exception {
        SysJob db;
        String oldName = null;
        if (input.isNew()) {
            db = repository.save(input);
        } else {
            SysJob old = repository.findById(input.getId()).orElse(null);
            Assert.notNull(old, "任务不存在");
            oldName = old.getName();
            this.updateField(input, requestKeys); // NOSONAR: save() 已开启事务
            db = repository.findById(input.getId()).orElse(null);
        }

        // 改名场景：先按变更前的旧 name 清理 Quartz 任务/触发器，避免残留任务继续触发
        quartzService.deleteJobByName(oldName);
        quartzService.deleteJob(db);
        if (db.getEnabled()) {
            quartzService.scheduleJob(db);
        }

        return null;
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(String id) throws SchedulerException {
        log.info("删除定时任务 {}", id);
        SysJob job = repository.findById(id).orElse(null);
        Assert.notNull(job, "该任务已被删除，请勿重复操作");
        quartzService.deleteJob(job);

        sysJobLogRepository.deleteBySysJobId(id);

        deleteById(id); // NOSONAR: deleteJob 已开启事务
    }


    public Page<SysJobLog> findAllExecuteRecord(Specification<SysJobLog> q, Pageable pageable) {
        return sysJobLogRepository.findAll(q, pageable);
    }

    public Page<SysJob> page(String name, String jobClass, Pageable pageable) throws SchedulerException {
        return repository.findAll(Spec.<SysJob>of().like(SysJob.Fields.name, name).like(SysJob.Fields.jobClass, jobClass), pageable);
    }
}
