package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysManual;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public interface SysManualRepository extends BaseRepository<SysManual, String> {

    SysManual findTop1ByNameOrderByVersionDesc(String name);

    default int findMaxVersion(String name) {
        SysManual e = this.findTop1ByNameOrderByVersionDesc(name);
        return e == null ? 0 : e.getVersion();
    }

}

