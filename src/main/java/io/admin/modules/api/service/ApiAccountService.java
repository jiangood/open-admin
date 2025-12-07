package io.admin.modules.api.service;

import io.admin.framework.data.service.BaseService;
import io.admin.modules.api.dao.ApiAccountDao;
import io.admin.modules.api.entity.ApiAccount;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ApiAccountService extends BaseService<ApiAccount> {

    @Resource
    ApiAccountDao apiAccountDao;

    public ApiAccount findByAppId(String appId) {
        return apiAccountDao.findByField(ApiAccount.Fields.appId, appId);
    }
}
