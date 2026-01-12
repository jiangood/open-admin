package io.github.jiangood.as.modules.system.dao;

import io.github.jiangood.as.framework.data.repository.BaseDao;
import io.github.jiangood.as.modules.system.entity.SysFile;
import org.springframework.stereotype.Repository;

@Repository
public class SysFileDao extends BaseDao<SysFile> {

    public SysFile findByTradeNo(String tradeNo) {
        return this.findByField(SysFile.Fields.tradeNo, tradeNo);
    }
}
