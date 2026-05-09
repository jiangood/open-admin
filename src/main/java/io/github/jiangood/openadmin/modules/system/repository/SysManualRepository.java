package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysManual;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysManualRepository extends BaseRepository<SysManual, String> {

    Optional<SysManual> findTop1ByNameOrderByVersionDesc(String name);

    default int findMaxVersion(String name) {
        return this.findTop1ByNameOrderByVersionDesc(name)
                .map(SysManual::getVersion)
                .orElse(0);
    }

}

