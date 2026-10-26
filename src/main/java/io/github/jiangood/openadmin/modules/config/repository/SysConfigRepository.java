package io.github.jiangood.openadmin.modules.config.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.config.entity.SysConfig;

import java.util.Collection;
import java.util.Optional;

public interface SysConfigRepository extends BaseRepository<SysConfig, String> {

    Optional<SysConfig> findByConfigKey(String configKey);

    void deleteByConfigKeyIn(Collection<String> configKeys);
}
