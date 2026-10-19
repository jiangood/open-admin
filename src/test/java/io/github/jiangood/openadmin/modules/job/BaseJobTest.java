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
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        verify(jobProgressRegistry).update("job-1", 3L, 10L, "已处理 3/10");
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
    void execute_dailyAggregation_firstRunShouldCreateBucketLog() throws Exception {
        Date fireTime = Date.from(Instant.parse("2026-10-19T10:15:30Z"));
        stubContext(fireTime);
        String bucket = bucketOf(fireTime, "yyyy-MM-dd");
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob(JobLogAggregation.DAILY));
        when(sysJobLogRepository.findFirstBySysJob_IdAndLogBucket("job-1", bucket)).thenReturn(Optional.empty());

        job.execute(context);

        ArgumentCaptor<SysJobLog> captor = ArgumentCaptor.forClass(SysJobLog.class);
        verify(sysJobLogRepository, times(2)).save(captor.capture());
        SysJobLog saved = captor.getValue();
        assertEquals(bucket, saved.getLogBucket());
        assertEquals(1, saved.getExecuteCount());
        assertEquals(0, saved.getFailCount());
        assertTrue(saved.getSuccess());
    }

    @Test
    void execute_dailyAggregation_shouldReuseExistingBucketLog() throws Exception {
        Date fireTime = Date.from(Instant.parse("2026-10-19T10:15:30Z"));
        stubContext(fireTime);
        String bucket = bucketOf(fireTime, "yyyy-MM-dd");
        LocalDateTime begin = LocalDateTime.ofInstant(fireTime.toInstant(), ZoneId.systemDefault()).minusMinutes(5);

        SysJobLog existing = new SysJobLog();
        existing.setId("log-9");
        existing.setBeginTime(begin);
        existing.setExecuteCount(1);
        existing.setFailCount(0);
        existing.setTotalRunTime(10L);

        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob(JobLogAggregation.DAILY));
        when(sysJobLogRepository.findFirstBySysJob_IdAndLogBucket("job-1", bucket)).thenReturn(Optional.of(existing));

        job.execute(context);

        ArgumentCaptor<SysJobLog> captor = ArgumentCaptor.forClass(SysJobLog.class);
        verify(sysJobLogRepository).save(captor.capture());
        SysJobLog saved = captor.getValue();
        assertEquals("log-9", saved.getId());
        assertEquals(2, saved.getExecuteCount());
        assertTrue(saved.getSuccess());
        assertTrue(saved.getTotalRunTime() >= 10L);
        // 复用周期记录时保留第一次执行时间
        assertEquals(begin, saved.getBeginTime());
    }

    @Test
    void execute_hourlyAggregation_shouldUseHourBucket() throws Exception {
        Date fireTime = Date.from(Instant.parse("2026-10-19T10:15:30Z"));
        stubContext(fireTime);
        String bucket = bucketOf(fireTime, "yyyy-MM-dd-HH");
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob(JobLogAggregation.HOURLY));
        when(sysJobLogRepository.findFirstBySysJob_IdAndLogBucket("job-1", bucket)).thenReturn(Optional.empty());

        job.execute(context);

        verify(sysJobLogRepository).findFirstBySysJob_IdAndLogBucket("job-1", bucket);
    }

    @Test
    void execute_aggregation_shouldCountFailure() throws Exception {
        Date fireTime = Date.from(Instant.parse("2026-10-19T10:15:30Z"));
        stubContext(fireTime);
        String bucket = bucketOf(fireTime, "yyyy-MM-dd");

        SysJobLog existing = new SysJobLog();
        existing.setId("log-9");
        existing.setExecuteCount(3);
        existing.setFailCount(0);
        existing.setTotalRunTime(100L);

        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob(JobLogAggregation.DAILY));
        when(sysJobLogRepository.findFirstBySysJob_IdAndLogBucket("job-1", bucket)).thenReturn(Optional.of(existing));
        job.fail = true;

        job.execute(context);

        ArgumentCaptor<SysJobLog> captor = ArgumentCaptor.forClass(SysJobLog.class);
        verify(sysJobLogRepository).save(captor.capture());
        SysJobLog saved = captor.getValue();
        assertEquals(4, saved.getExecuteCount());
        assertEquals(1, saved.getFailCount());
        assertFalse(saved.getSuccess());
    }

    private static String bucketOf(Date fireTime, String pattern) {
        return LocalDateTime.ofInstant(fireTime.toInstant(), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    static class TestJob extends BaseJob {

        boolean fail;
        boolean error;

        @Override
        public String execute(JobDataMap data, Logger logger) throws Exception {
            progress(3, 10, "已处理 3/10");
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
