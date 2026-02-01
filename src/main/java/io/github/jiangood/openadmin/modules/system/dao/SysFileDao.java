package io.github.jiangood.openadmin.modules.system.dao;

import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import org.springframework.stereotype.Repository;

@Repository
public class SysFileDao extends BaseDao<SysFile> {

    public SysFile findByTradeNo(String tradeNo) {
        return this.findByField(SysFile.Fields.tradeNo, tradeNo);
    }
}
