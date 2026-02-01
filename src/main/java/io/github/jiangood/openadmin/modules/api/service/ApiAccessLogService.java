package io.github.jiangood.openadmin.modules.api.service;


import io.github.jiangood.openadmin.lang.JsonTool;
import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.api.dao.ApiAccessLogDao;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccessLog;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccount;
import io.github.jiangood.openadmin.modules.api.entity.ApiResource;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ApiAccessLogService extends BaseService<ApiAccessLog> {

    @Resource
    ApiAccessLogDao dao;

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

        dao.save(a);
    }
}

