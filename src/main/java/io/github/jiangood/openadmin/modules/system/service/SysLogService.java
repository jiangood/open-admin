package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SysLogService extends BaseService<SysLog> {

    @Async("operationLogExecutor")
    public void saveOperationLogAsync(SysLog sysLog) {
        repository.save(sysLog);
    }

    /**
     * 分页查询某个用户的操作记录（个人中心"最近操作"）。
     */
    public Page<SysLog> pageByUser(String userId, Pageable pageable) {
        Spec<SysLog> q = Spec.of();
        q.eq(SysLog.Fields.userId, userId);
        return findAll(q, pageable);
    }

    @Transactional
    public SysLog save(SysLog input, List<String> requestKeys) {
        if (input.isNew()) {
            return repository.save(input);
        }

        this.updateField(input, requestKeys);
        return repository.findById(input.getId()).orElse(null);
    }
}
