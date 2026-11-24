package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.modules.job.JobStatus;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 僵死执行记录修正：数据库里是 {@link JobStatus#RUNNING}、但 Quartz 调度器中已经不存在这次执行，
 * 说明这条记录不可能再被更新（进程崩溃残留、结束写库失败等），直接标记为异常。
 *
 * <p>比对依据是 Quartz 的触发实例 ID（{@code fireInstanceId}），与任务名无关，
 * 任务改名不影响判断；同一任务也不会并发执行（{@code @DisallowConcurrentExecution}）。</p>
 *
 * <p>启动完成时检查一次；之后每次任务执行结束再检查一次，成本是一条按状态索引的查询。
 * 任务线程卡住但仍在调度器中的情况不在此处理（它确实还在运行），可在管理页手动取消。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobStaleLogSweeper {

    private final SysJobLogRepository sysJobLogRepository;
    /**
     * 延迟获取 Scheduler：本类被 Quartz 全局监听器依赖，直接注入会形成循环依赖
     */
    private final ObjectProvider<Scheduler> schedulerProvider;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        sweep();
    }

    public void sweep() {
        try {
            Scheduler scheduler = schedulerProvider.getIfAvailable();
            if (scheduler == null) {
                return;
            }
            List<SysJobLog> running = sysJobLogRepository.findByStatus(JobStatus.RUNNING);
            if (running.isEmpty()) {
                return;
            }
            Set<String> executing = scheduler.getCurrentlyExecutingJobs().stream()
                    .map(JobExecutionContext::getFireInstanceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<String> staleIds = running.stream()
                    .filter(log -> log.getFireInstanceId() == null
                            || !executing.contains(log.getFireInstanceId()))
                    .map(SysJobLog::getId)
                    .toList();
            if (staleIds.isEmpty()) {
                return;
            }
            int count = sysJobLogRepository.markStaleByIds(staleIds, JobStatus.EXCEPTION,
                    "执行中断（调度器中已无此任务）", LocalDateTime.now());
            if (count > 0) {
                log.warn("修正 {} 条僵死的运行中执行记录", count);
            }
        } catch (Exception e) {
            log.warn("检查僵死任务记录失败: {}", e.getMessage());
        }
    }
}
