package io.github.jiangood.openadmin.modules.job.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 任务的全部执行记录 ID，用于按 logId 关联日志文件做占用统计（不加载实体）
     */
    @Query("select l.id from SysJobLog l where l.sysJob.id = :jobId")
    List<String> findIdsByJobId(@Param("jobId") String jobId);

    /**
     * 指定时间之后开始的执行次数合计（按聚合记录的 executeCount 累计）
     */
    @Query("select coalesce(sum(l.executeCount), 0) from SysJobLog l where l.beginTime >= :start")
    Long sumExecuteCountSince(@Param("start") LocalDateTime start);

    /**
     * 指定时间之后开始的执行中失败次数合计
     */
    @Query("select coalesce(sum(l.failCount), 0) from SysJobLog l where l.beginTime >= :start")
    Long sumFailCountSince(@Param("start") LocalDateTime start);
}
