package io.github.jiangood.openadmin.modules.api.service;


import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import io.github.jiangood.openadmin.modules.api.entity.ApiLog;
import io.github.jiangood.openadmin.modules.api.repository.ApiLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiAccessLogService {

    private final ApiLogRepository apiAccessLogRepository;

    public void add(long timestamp, ApiAccount account, String url, String ip, long executionTime) {
        ApiLog a = new ApiLog();
        a.setTimestamp(timestamp);
        a.setUrl(url);
        a.setIp(ip);


        // a.setIpLocation(ip);

        a.setExecutionTime(executionTime);
        a.setAccountName(account.getName());
        apiAccessLogRepository.save(a);
    }

    // BaseService 方法
    @Transactional
    public ApiLog save(ApiLog input, List<String> requestKeys) throws Exception {
        if (input.isNew()) {
            return apiAccessLogRepository.save(input);
        }

        apiAccessLogRepository.updateField(input, requestKeys);
        return apiAccessLogRepository.findOne(input.getId());
    }

    @Transactional
    public void deleteById(String id) {
        apiAccessLogRepository.deleteById(id);
    }

    public Page<ApiLog> findAll(Specification<ApiLog> spec, Pageable pageable) {
        return apiAccessLogRepository.findAll(spec, pageable);
    }

    public List<ApiLog> findAll() {
        return apiAccessLogRepository.findAll();
    }

    public List<ApiLog> findAll(Sort sort) {
        return apiAccessLogRepository.findAll(sort);
    }

    public List<ApiLog> findAll(Specification<ApiLog> s, Sort sort) {
        return apiAccessLogRepository.findAll(s, sort);
    }

    public ApiLog findById(String id) {
        return apiAccessLogRepository.findOne(id);
    }

    public Spec<ApiLog> spec() {
        return Spec.of();
    }

    public ApiLog save(ApiLog t) {
        return apiAccessLogRepository.save(t);
    }

}

