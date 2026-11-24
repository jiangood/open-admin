package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.modules.job.JobStatus;
import io.github.jiangood.openadmin.modules.job.dto.JobExecStat;
import io.github.jiangood.openadmin.modules.job.dto.JobStat;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobStatServiceTest {

    @Mock
    private SysJobRepository sysJobRepository;
    @Mock
    private SysJobLogRepository sysJobLogRepository;
    @Mock
    private JobLogSizeService jobLogSizeService;
    @Mock
    private Scheduler scheduler;

    private JobStatService service;

    @BeforeEach
    void setUp() {
        service = new JobStatService(sysJobRepository, sysJobLogRepository,
                jobLogSizeService, scheduler);
    }

    @Test
    void stat_shouldAggregateTaskCountsAndSchedulerInfo() throws Exception {
        when(sysJobRepository.count()).thenReturn(5L);
        when(sysJobRepository.countByEnabled(true)).thenReturn(3L);
        when(sysJobLogRepository.countGroupByStatusSince(any())).thenReturn(List.of(
                row(JobStatus.SUCCESS, 8),
                row(JobStatus.SKIPPED, 1),
                row(JobStatus.PARTIAL, 1),
                row(JobStatus.FAILED, 1),
                row(JobStatus.EXCEPTION, 1),
                row(JobStatus.RUNNING, 1)));

        SchedulerMetaData meta = mock(SchedulerMetaData.class);
        when(scheduler.getMetaData()).thenReturn(meta);
        when(meta.getVersion()).thenReturn("2.5.0");
        when(meta.getSchedulerName()).thenReturn("scheduler");
        when(meta.getSchedulerInstanceId()).thenReturn("non_clustered");
        when(meta.isShutdown()).thenReturn(false);
        when(meta.getRunningSince()).thenReturn(new java.util.Date());
        when(meta.isInStandbyMode()).thenReturn(true);
        when(meta.getNumberOfJobsExecuted()).thenReturn(42);
        when(meta.getThreadPoolSize()).thenReturn(4);
        org.mockito.Mockito.doReturn(Object.class).when(meta).getJobStoreClass();
        when(meta.isJobStoreSupportsPersistence()).thenReturn(true);
        when(meta.isJobStoreClustered()).thenReturn(false);

        JobStat stat = service.stat();

        assertEquals(5, stat.getJobCount());
        assertEquals(3, stat.getEnabledCount());
        // 今日执行包含运行中与跳过
        assertEquals(13, stat.getTodayExecuteCount());
        // 失败口径：部分成功 + 失败 + 异常 + 取消
        assertEquals(3, stat.getTodayFailCount());

        JobStat.Scheduler schedulerInfo = stat.getScheduler();
        assertNotNull(schedulerInfo);
        assertEquals("2.5.0", schedulerInfo.getVersion());
        assertEquals("scheduler", schedulerInfo.getSchedulerName());
        assertTrue(schedulerInfo.isStarted());
        assertTrue(schedulerInfo.isStandby());
        assertNotNull(schedulerInfo.getRunningSince());
        assertEquals(42, schedulerInfo.getJobsExecuted());
        assertEquals(4, schedulerInfo.getThreadPoolSize());
        assertEquals("Object", schedulerInfo.getJobStoreClass());
        assertTrue(schedulerInfo.isPersistent());
        assertFalse(schedulerInfo.isClustered());
    }

    @Test
    void stat_shouldExposeErrorWhenSchedulerUnavailable() throws Exception {
        when(scheduler.getMetaData()).thenThrow(new SchedulerException("boom"));

        JobStat stat = service.stat();

        assertEquals("boom", stat.getScheduler().getError());
        assertNull(stat.getScheduler().getVersion());
    }

    @Test
    void jobExecStat_shouldGroupByStatusAndExposeLastRecord() {
        when(sysJobLogRepository.countGroupByStatusByJobId("job-1")).thenReturn(List.of(
                row(JobStatus.SUCCESS, 5),
                row(JobStatus.PARTIAL, 1),
                row(JobStatus.SKIPPED, 2),
                row(JobStatus.FAILED, 1),
                row(JobStatus.EXCEPTION, 1),
                row(JobStatus.CANCELLED, 1),
                row(JobStatus.RUNNING, 1)));

        SysJobLog last = new SysJobLog();
        last.setStatus(JobStatus.FAILED);
        last.setResult("余额不平");
        last.setBeginTime(LocalDateTime.of(2026, 11, 24, 10, 0, 0));
        last.setEndTime(LocalDateTime.of(2026, 11, 24, 10, 0, 5));
        last.setJobRunTime(5000L);
        when(sysJobLogRepository.findFirstBySysJob_IdOrderByBeginTimeDesc("job-1"))
                .thenReturn(Optional.of(last));

        JobExecStat stat = service.jobExecStat("job-1");

        assertEquals(12, stat.getRecordCount());
        assertEquals(5, stat.getSuccessCount());
        assertEquals(1, stat.getPartialCount());
        assertEquals(2, stat.getSkipCount());
        assertEquals(1, stat.getFailCount());
        assertEquals(1, stat.getExceptionCount());
        assertEquals(1, stat.getCancelledCount());
        assertEquals(1, stat.getRunningCount());
        assertEquals(JobStatus.FAILED, stat.getLastStatus());
        assertEquals("余额不平", stat.getLastResult());
        assertEquals("2026-11-24 10:00:00", stat.getLastBeginTime());
        assertEquals("2026-11-24 10:00:05", stat.getLastEndTime());
        assertEquals(5000L, stat.getLastRunTime());
    }

    private static Object[] row(JobStatus status, long count) {
        return new Object[]{status, count};
    }
}
