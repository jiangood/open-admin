package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.modules.job.dto.JobStat;
import io.github.jiangood.openadmin.modules.job.progress.JobProgress;
import io.github.jiangood.openadmin.modules.job.progress.JobProgressRegistry;
import io.github.jiangood.openadmin.modules.job.progress.JobRunState;
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
    private JobProgressRegistry jobProgressRegistry;
    @Mock
    private JobLogSizeService jobLogSizeService;
    @Mock
    private Scheduler scheduler;

    private JobStatService service;

    @BeforeEach
    void setUp() {
        service = new JobStatService(sysJobRepository, sysJobLogRepository,
                jobProgressRegistry, jobLogSizeService, scheduler);
    }

    @Test
    void stat_shouldAggregateTaskCountsAndSchedulerInfo() throws Exception {
        when(sysJobRepository.count()).thenReturn(5L);
        when(sysJobRepository.countByEnabled(true)).thenReturn(3L);
        when(jobProgressRegistry.listAll()).thenReturn(List.of(progress(JobRunState.RUNNING), progress(JobRunState.SUCCESS)));
        when(sysJobLogRepository.sumExecuteCountSince(any())).thenReturn(12L);
        when(sysJobLogRepository.sumFailCountSince(any())).thenReturn(null);

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
        assertEquals(1, stat.getRunningCount());
        assertEquals(12, stat.getTodayExecuteCount());
        assertEquals(0, stat.getTodayFailCount());

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

    private static JobProgress progress(JobRunState state) {
        JobProgress progress = new JobProgress();
        progress.setState(state);
        progress.setBeginTime(LocalDateTime.now());
        return progress;
    }
}
