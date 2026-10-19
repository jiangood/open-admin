package io.github.jiangood.openadmin.modules.job.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SysJobLogRepository extends BaseRepository<SysJobLog, String> {

    void deleteBySysJobId(String jobId);

    List<SysJobLog> findBySysJobId(String jobId);

    /**
     * 查询指定任务在某个聚合桶内的执行记录（按小时/按天聚合时复用）
     */
    Optional<SysJobLog> findFirstBySysJob_IdAndLogBucket(String sysJobId, String logBucket);

    Page<SysJobLog> findByUpdateTimeBefore(LocalDateTime deadline, Pageable pageable);
}
