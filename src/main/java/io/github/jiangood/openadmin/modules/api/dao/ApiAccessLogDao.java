package io.github.jiangood.openadmin.modules.api.dao;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccessLog;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiAccessLogDao extends BaseRepository<ApiAccessLog, String> {

}

