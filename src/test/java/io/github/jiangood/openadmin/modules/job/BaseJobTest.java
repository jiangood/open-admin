package io.github.jiangood.openadmin.modules.job;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
    private JobExecutionContext context;
    @Mock
    private JobDetail jobDetail;

    private TestJob job;

    /**
     * save 时传入对象的快照（实体会被后续更新，直接捕获引用拿到的是最终状态）
     */
    private final List<JobStatus> savedStatuses = new ArrayList<>();

    @BeforeEach
    void setUp() {
        job = new TestJob();
        ReflectionTestUtils.setField(job, "sysJobRepository", sysJobRepository);
        ReflectionTestUtils.setField(job, "sysJobLogRepository", sysJobLogRepository);
    }

    private void stubContext() {
        stubContext(new Date());
    }

    private void stubContext(Date fireTime) {
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("job-1"));
        when(context.getFireTime()).thenReturn(fireTime);
        when(context.getFireInstanceId()).thenReturn("fire-1");
        when(sysJobLogRepository.save(any())).thenAnswer(inv -> {
            SysJobLog log = inv.getArgument(0);
            savedStatuses.add(log.getStatus());
            if (log.getId() == null) {
                log.setId("log-1");
            }
            return log;
        });
    }

    private SysJob existingJob() {
        SysJob sysJob = new SysJob("job-1");
        sysJob.setName("job-1");
        return sysJob;
    }

    private List<SysJobLog> executeAndCapture() throws Exception {
        job.execute(context);
        ArgumentCaptor<SysJobLog> captor = ArgumentCaptor.forClass(SysJobLog.class);
        verify(sysJobLogRepository, times(2)).save(captor.capture());
        return captor.getAllValues();
    }

    @Test
    void execute_shouldWriteRunningThenSuccess() throws Exception {
        stubContext();
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob());

        List<SysJobLog> saved = executeAndCapture();

        assertEquals(JobStatus.RUNNING, savedStatuses.get(0));
        assertEquals(JobStatus.SUCCESS, savedStatuses.get(1));
        assertEquals("fire-1", saved.get(0).getFireInstanceId());
        SysJobLog finished = saved.get(1);
        assertEquals(JobStatus.SUCCESS, finished.getStatus());
        assertEquals("OK", finished.getResult());
        assertNotNull(finished.getEndTime());
        assertNotNull(finished.getJobRunTime());
    }

    @Test
    void execute_shouldKeepPartialStatus() throws Exception {
        stubTestResult(JobResult.partial("3 个文件删除失败"));

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(JobStatus.PARTIAL, finished.getStatus());
        assertEquals("3 个文件删除失败", finished.getResult());
    }

    @Test
    void execute_shouldKeepSkippedStatus() throws Exception {
        stubTestResult(JobResult.skip("非交易日"));

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(JobStatus.SKIPPED, finished.getStatus());
        assertEquals("非交易日", finished.getResult());
    }

    @Test
    void execute_shouldKeepFailedStatus() throws Exception {
        stubTestResult(JobResult.fail("余额不平"));

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(JobStatus.FAILED, finished.getStatus());
        assertEquals("余额不平", finished.getResult());
    }

    @Test
    void execute_shouldUseStatusLabelWhenMessageBlank() throws Exception {
        stubTestResult(JobResult.ok("  "));

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(JobStatus.SUCCESS, finished.getStatus());
        assertEquals("成功", finished.getResult());
    }

    @Test
    void execute_shouldTruncateLongMessage() throws Exception {
        String longMessage = "x".repeat(300);
        stubTestResult(JobResult.ok(longMessage));

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(240, finished.getResult().length());
    }

    @Test
    void execute_shouldMarkExceptionWhenJobThrows() throws Exception {
        stubContext();
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob());
        job.mode = TestJob.Mode.THROW;

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(JobStatus.EXCEPTION, finished.getStatus());
        assertEquals("boom", finished.getResult());
    }

    @Test
    void execute_shouldMarkExceptionWhenResultIsNull() throws Exception {
        stubTestResult(null);

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(JobStatus.EXCEPTION, finished.getStatus());
        assertEquals("任务执行完成但未返回结果", finished.getResult());
    }

    @Test
    void execute_shouldRejectRunningResult() throws Exception {
        stubTestResult(new JobResult(JobStatus.RUNNING, "假装运行中"));

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(JobStatus.EXCEPTION, finished.getStatus());
        assertEquals("任务不能返回运行中状态", finished.getResult());
    }

    @Test
    void execute_shouldRejectNullStatus() throws Exception {
        stubTestResult(new JobResult(null, "缺状态"));

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(JobStatus.EXCEPTION, finished.getStatus());
        assertEquals("返回结果缺少状态", finished.getResult());
    }

    @Test
    void execute_shouldMarkCancelledWhenInterrupted() throws Exception {
        stubTestResult(JobResult.ok());
        job.mode = TestJob.Mode.INTERRUPT;

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(JobStatus.CANCELLED, finished.getStatus());
        assertEquals("已取消", finished.getResult());
        assertTrue(Thread.interrupted(), "取消后应保留中断标记");
    }

    @Test
    void execute_shouldMarkExceptionAndRethrowError() throws Exception {
        stubTestResult(JobResult.ok());
        job.mode = TestJob.Mode.ERROR;

        assertThrows(OutOfMemoryError.class, () -> job.execute(context));

        ArgumentCaptor<SysJobLog> captor = ArgumentCaptor.forClass(SysJobLog.class);
        verify(sysJobLogRepository, times(2)).save(captor.capture());
        SysJobLog finished = captor.getAllValues().get(1);
        assertEquals(JobStatus.EXCEPTION, finished.getStatus());
        assertEquals("boom", finished.getResult());
    }

    @Test
    void execute_shouldSkipWhenJobDefinitionMissing() {
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey("job-1"));
        when(sysJobRepository.findByName("job-1")).thenReturn(null);

        job.execute(context);

        verify(sysJobLogRepository, never()).save(any());
    }

    @Test
    void execute_shouldSetBeginTimeToFireTime() throws Exception {
        Date fireTime = Date.from(Instant.parse("2026-10-19T10:15:30Z"));
        stubContext(fireTime);
        LocalDateTime begin = LocalDateTime.ofInstant(fireTime.toInstant(), ZoneId.systemDefault());
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob());

        SysJobLog finished = executeAndCapture().get(1);

        assertEquals(begin, finished.getBeginTime());
    }

    private void stubTestResult(JobResult result) {
        stubContext();
        when(sysJobRepository.findByName("job-1")).thenReturn(existingJob());
        job.result = result;
        job.mode = TestJob.Mode.RETURN;
    }

    static class TestJob extends BaseJob {

        enum Mode {RETURN, THROW, ERROR, INTERRUPT}

        Mode mode = Mode.RETURN;
        JobResult result = JobResult.ok("OK");

        @Override
        public JobResult execute(JobDataMap data, Logger logger) throws Exception {
            switch (mode) {
                case THROW -> throw new IllegalStateException("boom");
                case ERROR -> throw new OutOfMemoryError("boom");
                case INTERRUPT -> throw new InterruptedException("cancelled");
                default -> {
                    return result;
                }
            }
        }
    }
}
