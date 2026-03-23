package io.github.jiangood.openadmin.modules.api.service;


import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import io.github.jiangood.openadmin.modules.api.entity.ApiLog;
import io.github.jiangood.openadmin.modules.api.repository.ApiLogRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApiAccessLogService {

    @Resource
    ApiLogRepository apiAccessLogRepository;

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
    public void delete(String id) {
        apiAccessLogRepository.deleteById(id);
    }

    public Page<ApiLog> getPage(Specification<ApiLog> spec, Pageable pageable) {
        return apiAccessLogRepository.findAll(spec, pageable);
    }

    public ApiLog detail(String id) {
        return apiAccessLogRepository.findOne(id);
    }

    public ApiLog get(String id) {
        return apiAccessLogRepository.findOne(id);
    }

    public List<ApiLog> getAll() {
        return apiAccessLogRepository.findAll();
    }

    public List<ApiLog> getAll(Sort sort) {
        return apiAccessLogRepository.findAll(sort);
    }

    public List<ApiLog> getAll(Specification<ApiLog> s, Sort sort) {
        return apiAccessLogRepository.findAll(s, sort);
    }

    public Spec<ApiLog> spec() {
        return Spec.of();
    }

    public ApiLog save(ApiLog t) {
        return apiAccessLogRepository.save(t);
    }

}

