package io.admin.modules.job.dao;

import io.admin.framework.data.repository.BaseDao;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.job.entity.SysJob;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SysJobDao extends BaseDao<SysJob> {

    public List<SysJob> findAllEnabled() {
        Spec<SysJob> q = spec();
        q.eq(SysJob.Fields.enabled, true);


        return this.findAll(q);
    }

    public SysJob findByName(String name) {
        Spec<SysJob> q = spec();
        q.eq(SysJob.Fields.name, name);


        return this.findOne(q);
    }

    public SysJob findByNameAndGroup(String name) {
        Spec<SysJob> q = spec();
        q.eq(SysJob.Fields.name, name);

        return this.findOne(q);
    }

    @Transactional
    public void save(String name, String paramKey, Object paramValue) {
        SysJob job = this.findByNameAndGroup(name);
        Assert.notNull(job, "没有找到数据库中的job " + name);

        Map<String, Object> jobData = job.getJobData();
        if(jobData == null){
            jobData = new HashMap<>();
            job.setJobData(jobData);
        }
        jobData.put(paramKey, paramValue);
        this.save(job);
    }
}
