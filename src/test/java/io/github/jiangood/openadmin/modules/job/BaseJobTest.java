package io.github.jiangood.openadmin.modules.job;

import io.github.jiangood.openadmin.modules.job.entity.JobLogAggregation;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.progress.JobProgressRegistry;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseJobTest {

    @Mock
    private SysJobRepository sysJobRepository;
    @Mock
    private SysJobLogRepository sysJobLogRepository;
    @Mock
    private JobProgressRegistry jobProgressRegistry;
    @Mock
    private JobExecutionContext context;
    @Mock
    private JobDetail jobDetail;

    private TestJob job;

    @BeforeEach
    void setUp() {
        job = new TestJob();
        ReflectionTestUtils.setField(job, "sysJobRepository", sysJobRepository);
        ReflectionTestUtils.setField(job, "sysJobLogRepository", sysJobLogRepository);
        ReflectionTestUtils.setField(job, "jobProgressRegistry", jobProgressRegistry);
    }

    private void stubContext() {
        stubContext(new Date());
    }

    private void stubContext(Date fireTime) {
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("job-1"));
        when(context.getFireTime()).thenReturn(fireTime);
        when(sysJobLogRepository.save(any())).thenAnswer(inv -> {
            SysJobLog log = inv.getArgument(0);
            if (log.getId() == null) {
                log.setId("log-1");
            }
            return log;
        });
    }

    private SysJob existingJob() {
        return existingJob(null);
    }

    private SysJob existingJob(JobLogAggregation aggregation) {
        SysJob sysJob = new SysJob("job-1");
        sysJob.setName("job-1");
        sysJob.setLogAggregation(aggregation);
        return sysJob;
    }

    @Test
    void execute_shouldReportStartAndSuccess() throws Exception {
        stubContext();
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob());

        job.execute(context);

        verify(jobProgressRegistry).start(any(SysJob.class), eq("log-1"));
        verify(jobProgressRegistry).success("job-1", "OK");
        verify(jobProgressRegistry).failIfRunning("job-1", "任务异常终止");
        verify(sysJobLogRepository, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    void execute_shouldReportFailWhenJobThrows() throws Exception {
        stubContext();
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob());
        job.fail = true;

        job.execute(context);

        verify(jobProgressRegistry).fail(eq("job-1"), org.mockito.ArgumentMatchers.startsWith("异常"));
        verify(jobProgressRegistry, never()).success(anyString(), anyString());
    }

    @Test
    void execute_shouldSkipWhenJobDefinitionMissing() throws Exception {
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("job-1"));
        when(sysJobRepository.findByName("job-1")).thenReturn(null);

        job.execute(context);

        verify(jobProgressRegistry, never()).start(any(), anyString());
        verify(sysJobLogRepository, never()).save(any());
    }

    @Test
    void progress_shouldForwardToRegistry() throws Exception {
        stubContext();
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob());

        job.execute(context);

        verify(jobProgressRegistry).update("job-1", "已处理 3/10");
    }

    @Test
    void execute_shouldMarkFailedWhenErrorThrown() throws Exception {
        stubContext();
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob());
        job.error = true;

        try {
            job.execute(context);
        } catch (OutOfMemoryError ignored) {
            // 模拟 Error 向上冒泡
        }

        verify(jobProgressRegistry).failIfRunning("job-1", "任务异常终止");
    }

    @Test
    void execute_perRun_shouldNotUseBucket() throws Exception {
        stubContext();
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob());

        job.execute(context);

        verify(sysJobLogRepository, never()).findFirstBySysJob_IdAndLogBucket(anyString(), anyString());
        verify(sysJobLogRepository, times(2)).save(any());
    }

    @Test
    void execute_shouldIgnoreAggregationAndCreatePerRunLog() throws Exception {
        Date fireTime = Date.from(Instant.parse("2026-10-19T10:15:30Z"));
        stubContext(fireTime);
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob(JobLogAggregation.DAILY));

        job.execute(context);

        verify(sysJobLogRepository, never()).findFirstBySysJob_IdAndLogBucket(anyString(), anyString());
        ArgumentCaptor<SysJobLog> captor = ArgumentCaptor.forClass(SysJobLog.class);
        verify(sysJobLogRepository, times(2)).save(captor.capture());
        SysJobLog saved = captor.getValue();
        assertNull(saved.getLogBucket());
        assertEquals(1, saved.getExecuteCount());
        assertEquals(0, saved.getFailCount());
        assertTrue(saved.getSuccess());
    }

    @Test
    void execute_shouldSetBeginTimeToFireTime() throws Exception {
        Date fireTime = Date.from(Instant.parse("2026-10-19T10:15:30Z"));
        stubContext(fireTime);
        LocalDateTime begin = LocalDateTime.ofInstant(fireTime.toInstant(), ZoneId.systemDefault());
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob(JobLogAggregation.DAILY));

        job.execute(context);

        ArgumentCaptor<SysJobLog> captor = ArgumentCaptor.forClass(SysJobLog.class);
        verify(sysJobLogRepository, times(2)).save(captor.capture());
        assertEquals(begin, captor.getValue().getBeginTime());
    }

    @Test
    void execute_hourlyAggregation_shouldBeIgnored() throws Exception {
        Date fireTime = Date.from(Instant.parse("2026-10-19T10:15:30Z"));
        stubContext(fireTime);
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob(JobLogAggregation.HOURLY));

        job.execute(context);

        verify(sysJobLogRepository, never()).findFirstBySysJob_IdAndLogBucket(anyString(), anyString());
        ArgumentCaptor<SysJobLog> captor = ArgumentCaptor.forClass(SysJobLog.class);
        verify(sysJobLogRepository, times(2)).save(captor.capture());
        assertNull(captor.getValue().getLogBucket());
    }

    @Test
    void execute_shouldMarkPerRunLogFailedWhenJobThrows() throws Exception {
        Date fireTime = Date.from(Instant.parse("2026-10-19T10:15:30Z"));
        stubContext(fireTime);
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob(JobLogAggregation.DAILY));
        job.fail = true;

        job.execute(context);

        verify(sysJobLogRepository, never()).findFirstBySysJob_IdAndLogBucket(anyString(), anyString());
        ArgumentCaptor<SysJobLog> captor = ArgumentCaptor.forClass(SysJobLog.class);
        verify(sysJobLogRepository, times(2)).save(captor.capture());
        SysJobLog saved = captor.getValue();
        assertNull(saved.getLogBucket());
        assertEquals(1, saved.getExecuteCount());
        assertEquals(1, saved.getFailCount());
        assertFalse(saved.getSuccess());
    }

    static class TestJob extends BaseJob {

        boolean fail;
        boolean error;

        @Override
        public String execute(JobDataMap data, Logger logger) throws Exception {
            progress("已处理 3/10");
            if (error) {
                throw new OutOfMemoryError("boom");
            }
            if (fail) {
                throw new IllegalStateException("boom");
            }
            return "OK";
        }
    }
}
