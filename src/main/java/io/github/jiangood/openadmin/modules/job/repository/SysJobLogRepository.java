package io.github.jiangood.openadmin.modules.job.repository;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.job.JobStatus;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SysJobLogRepository extends BaseRepository<SysJobLog, String> {

    void deleteBySysJobId(String jobId);

    List<SysJobLog> findBySysJobId(String jobId);

    Page<SysJobLog> findByUpdateTimeBefore(LocalDateTime deadline, Pageable pageable);

    List<SysJobLog> findByStatus(JobStatus status);

    /**
     * 任务的全部执行记录 ID，用于按 logId 关联日志文件做占用统计（不加载实体）
     */
    @Query("select l.id from SysJobLog l where l.sysJob.id = :jobId")
    List<String> findIdsByJobId(@Param("jobId") String jobId);

    /**
     * 指定时间之后开始的执行记录，按状态分组计数（每行 {@code [JobStatus, Long]}）
     */
    @Query("select l.status, count(l) from SysJobLog l where l.beginTime >= :start group by l.status")
    List<Object[]> countGroupByStatusSince(@Param("start") LocalDateTime start);

    /**
     * 单个任务的执行记录，按状态分组计数（每行 {@code [JobStatus, Long]}）
     */
    @Query("select l.status, count(l) from SysJobLog l where l.sysJob.id = :jobId group by l.status")
    List<Object[]> countGroupByStatusByJobId(@Param("jobId") String jobId);

    Optional<SysJobLog> findFirstBySysJob_IdOrderByBeginTimeDesc(String jobId);

    /**
     * 把「记录为运行中、但调度器中已不存在」的记录标记为终态。
     */
    @Modifying
    @Transactional
    @Query("update SysJobLog l set l.status = :status, l.result = :result, l.endTime = :now " +
            "where l.id in :ids")
    int markStaleByIds(@Param("ids") List<String> ids,
                       @Param("status") JobStatus status,
                       @Param("result") String result,
                       @Param("now") LocalDateTime now);
}
