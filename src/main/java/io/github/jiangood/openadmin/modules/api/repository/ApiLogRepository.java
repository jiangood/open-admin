package io.github.jiangood.openadmin.modules.api.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.api.entity.ApiLog;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiLogRepository extends BaseRepository<ApiLog, String> {

}

