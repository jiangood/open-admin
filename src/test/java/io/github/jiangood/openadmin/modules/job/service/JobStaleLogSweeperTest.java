package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.modules.job.JobStatus;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobStaleLogSweeperTest {

    @Mock
    private SysJobLogRepository sysJobLogRepository;
    @Mock
    private ObjectProvider<Scheduler> schedulerProvider;
    @Mock
    private Scheduler scheduler;

    private JobStaleLogSweeper sweeper;

    @BeforeEach
    void setUp() {
        sweeper = new JobStaleLogSweeper(sysJobLogRepository, schedulerProvider);
    }

    @Test
    void sweep_shouldMarkLogsNotExecutingInScheduler() throws Exception {
        when(schedulerProvider.getIfAvailable()).thenReturn(scheduler);
        when(sysJobLogRepository.findByStatus(JobStatus.RUNNING))
                .thenReturn(List.of(runningLog("log-1", "fire-1"), runningLog("log-2", "fire-2")));
        JobExecutionContext executing = executing("fire-1");
        when(scheduler.getCurrentlyExecutingJobs()).thenReturn(List.of(executing));
        when(sysJobLogRepository.markStaleByIds(any(), any(), anyString(), any())).thenReturn(1);

        sweeper.sweep();

        verify(sysJobLogRepository).markStaleByIds(
                eq(List.of("log-2")), eq(JobStatus.EXCEPTION), anyString(), any(LocalDateTime.class));
    }

    @Test
    void sweep_shouldKeepLogsStillExecuting() throws Exception {
        when(schedulerProvider.getIfAvailable()).thenReturn(scheduler);
        when(sysJobLogRepository.findByStatus(JobStatus.RUNNING))
                .thenReturn(List.of(runningLog("log-1", "fire-1"), runningLog("log-2", "fire-2")));
        JobExecutionContext executing1 = executing("fire-1");
        JobExecutionContext executing2 = executing("fire-2");
        when(scheduler.getCurrentlyExecutingJobs()).thenReturn(List.of(executing1, executing2));

        sweeper.sweep();

        verify(sysJobLogRepository, never()).markStaleByIds(any(), any(), anyString(), any());
    }

    @Test
    void sweep_shouldMarkLogWithoutFireInstanceId() throws Exception {
        when(schedulerProvider.getIfAvailable()).thenReturn(scheduler);
        when(sysJobLogRepository.findByStatus(JobStatus.RUNNING))
                .thenReturn(List.of(runningLog("log-1", null)));
        when(scheduler.getCurrentlyExecutingJobs()).thenReturn(List.of());
        when(sysJobLogRepository.markStaleByIds(any(), any(), anyString(), any())).thenReturn(1);

        sweeper.sweep();

        // 没有触发实例 ID 的记录无法对应任何正在执行的实例，按僵死处理
        verify(sysJobLogRepository).markStaleByIds(
                eq(List.of("log-1")), eq(JobStatus.EXCEPTION), anyString(), any(LocalDateTime.class));
    }

    @Test
    void sweep_shouldSkipWhenNoRunningLogs() {
        when(schedulerProvider.getIfAvailable()).thenReturn(scheduler);
        when(sysJobLogRepository.findByStatus(JobStatus.RUNNING)).thenReturn(List.of());

        sweeper.sweep();

        verify(sysJobLogRepository, never()).markStaleByIds(any(), any(), anyString(), any());
    }

    @Test
    void sweep_shouldSkipWhenSchedulerMissing() {
        when(schedulerProvider.getIfAvailable()).thenReturn(null);

        sweeper.sweep();

        verifyNoInteractions(sysJobLogRepository);
    }

    @Test
    void sweep_shouldSwallowSchedulerError() throws Exception {
        when(schedulerProvider.getIfAvailable()).thenReturn(scheduler);
        when(sysJobLogRepository.findByStatus(JobStatus.RUNNING))
                .thenReturn(List.of(runningLog("log-1", "fire-1")));
        when(scheduler.getCurrentlyExecutingJobs()).thenThrow(new SchedulerException("boom"));

        assertDoesNotThrow(() -> sweeper.sweep());
        verify(sysJobLogRepository, never()).markStaleByIds(any(), any(), anyString(), any());
    }

    private static SysJobLog runningLog(String id, String fireInstanceId) {
        SysJobLog log = new SysJobLog();
        log.setId(id);
        log.setFireInstanceId(fireInstanceId);
        log.setStatus(JobStatus.RUNNING);
        return log;
    }

    private static JobExecutionContext executing(String fireInstanceId) {
        JobExecutionContext context = mock(JobExecutionContext.class);
        when(context.getFireInstanceId()).thenReturn(fireInstanceId);
        return context;
    }
}
