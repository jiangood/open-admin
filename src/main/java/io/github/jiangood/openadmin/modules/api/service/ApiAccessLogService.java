package io.github.jiangood.openadmin.modules.api.service;


import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccessLog;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import io.github.jiangood.openadmin.modules.api.entity.ApiResource;
import io.github.jiangood.openadmin.modules.api.repository.ApiAccessLogRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ApiAccessLogService {

    @Resource
    ApiAccessLogRepository apiAccessLogRepository;

    public void add(long timestamp, ApiAccount account, ApiResource resource, Map<String, Object> params, Object retValue, String ip, long executionTime) {
        ApiAccessLog a = new ApiAccessLog();
        a.setTimestamp(timestamp);
        a.setName(resource.getName());
        a.setAction(resource.getAction());
        a.setRequestData(JsonTool.toJsonQuietly(params));
        a.setResponseData(JsonTool.toJsonQuietly(retValue));
        a.setIp(ip);


        // a.setIpLocation(ip);

        a.setExecutionTime(executionTime);


        a.setAccountName(account.getName());

        apiAccessLogRepository.save(a);
    }

    // BaseService 方法
    @Transactional
    public ApiAccessLog save(ApiAccessLog input, List<String> requestKeys) throws Exception {
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

    public Page<ApiAccessLog> getPage(Specification<ApiAccessLog> spec, Pageable pageable) {
        return apiAccessLogRepository.findAll(spec, pageable);
    }

    public ApiAccessLog detail(String id) {
        return apiAccessLogRepository.findOne(id);
    }

    public ApiAccessLog get(String id) {
        return apiAccessLogRepository.findOne(id);
    }

    public List<ApiAccessLog> getAll() {
        return apiAccessLogRepository.findAll();
    }

    public List<ApiAccessLog> getAll(Sort sort) {
        return apiAccessLogRepository.findAll(sort);
    }

    public List<ApiAccessLog> getAll(Specification<ApiAccessLog> s, Sort sort) {
        return apiAccessLogRepository.findAll(s, sort);
    }

    public Spec<ApiAccessLog> spec() {
        return Spec.of();
    }

    public ApiAccessLog save(ApiAccessLog t) {
        return apiAccessLogRepository.save(t);
    }

}

