package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysLog;
import io.github.jiangood.openadmin.modules.system.repository.SysLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysLogService {

    private final SysLogRepository sysLogRepository;

    @Async("operationLogExecutor")
    public void saveOperationLogAsync(SysLog sysLog) {
        sysLogRepository.save(sysLog);
    }

    // BaseService 方法
    @Transactional
    public SysLog save(SysLog input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            return sysLogRepository.save(input);
        }

        sysLogRepository.updateField(input, requestKeys);
        return sysLogRepository.findById(input.getId()).orElse(null);
    }

    @Transactional
    public void deleteById(String id) {
        sysLogRepository.deleteById(id);
    }

    public Page<SysLog> findAll(Specification<SysLog> spec, Pageable pageable) {
        return sysLogRepository.findAll(spec, pageable);
    }

    public List<SysLog> findAll() {
        return sysLogRepository.findAll();
    }

    public List<SysLog> findAll(Sort sort) {
        return sysLogRepository.findAll(sort);
    }

    public List<SysLog> findAll(Specification<SysLog> s, Sort sort) {
        return sysLogRepository.findAll(s, sort);
    }

    public Spec<SysLog> spec() {
        return Spec.of();
    }

    public SysLog save(SysLog t) {
        return sysLogRepository.save(t);
    }

}
