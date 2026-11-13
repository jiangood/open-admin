package io.github.jiangood.openadmin.modules.task.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.task.entity.SysTaskLog;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysTaskLogRepository extends BaseRepository<SysTaskLog, String> {

    Optional<SysTaskLog> findByLogId(String logId);

}
