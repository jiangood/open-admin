package io.github.jiangood.as.modules.system.service;

import io.github.jiangood.as.framework.data.service.BaseService;
import io.github.jiangood.as.modules.system.dao.SysManualDao;
import io.github.jiangood.as.modules.system.entity.SysManual;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysManualService extends BaseService<SysManual> {

    @Resource
    SysManualDao dao;

    @Override
    public SysManual saveOrUpdateByRequest(SysManual input, List<String> updateKeys) throws Exception {
        if (input.isNew()) {
            int maxVersion = dao.findMaxVersion(input.getName());
            input.setVersion(maxVersion + 1);
        }

        return super.saveOrUpdateByRequest(input, updateKeys);
    }
}

