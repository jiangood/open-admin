package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysDictType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysDictTypeRepository extends BaseRepository<SysDictType, String> {
    Optional<SysDictType> findByTypeCode(String typeCode);
    Optional<SysDictType> findFirstByPidIsNull();
}
