package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.repository.SysLogRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SysLogService extends BaseService<SysLog> {

    public SysLogService(SysLogRepository repository, EntityManager entityManager) {
        super(repository, entityManager);
    }

    @Async("operationLogExecutor")
    public void saveOperationLogAsync(SysLog sysLog) {
        repository.save(sysLog);
    }

    @Transactional
    public SysLog save(SysLog input, List<String> requestKeys) {
        if (input.isNew()) {
            return repository.save(input);
        }

        this.updateField(input, requestKeys); // NOSONAR: save() 已开启事务
        return repository.findById(input.getId()).orElse(null); // NOSONAR: 非新实体路径下 id 必非空
    }
}
