package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import org.springframework.stereotype.Repository;

@Repository
public interface SysFileRepository extends BaseRepository<SysFile, String> {

    SysFile findByTradeNo(String tradeNo);
}
