package io.github.jiangood.openadmin.modules.api.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.api.entity.ApiAccessLog;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiAccessLogRepository extends BaseRepository<ApiAccessLog, String> {

}

