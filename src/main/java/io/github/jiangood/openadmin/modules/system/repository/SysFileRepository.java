package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysFileRepository extends BaseRepository<SysFile, String> {

    Optional<SysFile> findByTradeNo(String tradeNo);
}
