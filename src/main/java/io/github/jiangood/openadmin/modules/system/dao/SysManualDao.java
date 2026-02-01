package io.github.jiangood.openadmin.modules.system.dao;

import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysManual;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class SysManualDao extends BaseDao<SysManual> {


    public int findMaxVersion(String name) {
        Spec<SysManual> q = Spec.<SysManual>of().eq(SysManual.Fields.name, name);

        SysManual e = this.findTop1(q, Sort.by(Sort.Direction.DESC, SysManual.Fields.version));

        return e == null ? 0 : e.getVersion();
    }

}

