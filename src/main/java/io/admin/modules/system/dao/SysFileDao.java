
package io.admin.modules.system.dao;

import io.admin.modules.system.entity.SysFile;
import io.admin.framework.data.repository.BaseDao;
import org.springframework.stereotype.Repository;

@Repository
public class SysFileDao extends BaseDao<SysFile> {

    public SysFile findByTradeNo(String tradeNo) {
        return this.findByField(SysFile.Fields.tradeNo, tradeNo);
    }
}
