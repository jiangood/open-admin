package io.admin.modules.api.service;


import io.admin.common.utils.JsonUtils;
import io.admin.framework.data.service.BaseService;
import io.admin.modules.api.dao.ApiAccessLogDao;
import io.admin.modules.api.entity.ApiAccessLog;
import io.admin.modules.api.entity.ApiAccount;
import io.admin.modules.api.entity.ApiResource;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ApiAccessLogService extends BaseService<ApiAccessLog> {

    @Resource
    ApiAccessLogDao dao;

    public void add(long timestamp,ApiAccount account, ApiResource resource,   Map<String, Object> params, Object retValue,String ip, long executionTime) {
        ApiAccessLog a = new ApiAccessLog();
        a.setTimestamp(timestamp);
        a.setName(resource.getName());
        a.setAction(resource.getAction());
        a.setRequestData(JsonUtils.toJsonQuietly(params));
        a.setResponseData(JsonUtils.toJsonQuietly(retValue));
        a.setIp(ip);


        // a.setIpLocation(ip);

        a.setExecutionTime(executionTime);


        a.setAccountName(account.getName());

        dao.save(a);
    }
}

