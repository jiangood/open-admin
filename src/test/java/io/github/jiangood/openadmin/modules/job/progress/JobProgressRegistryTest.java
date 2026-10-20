package io.github.jiangood.openadmin.modules.job.progress;

import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobProgressRegistryTest {

    private JobProgressRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new JobProgressRegistry();
    }

    private SysJob job(String id, String name) {
        SysJob job = new SysJob(id);
        job.setName(name);
        return job;
    }

    @Test
    void start_shouldCreateRunningSnapshot() {
        registry.start(job("job-1", "同步"), "log-1");

        JobProgress p = registry.get("job-1");
        assertNotNull(p);
        assertEquals(JobRunState.RUNNING, p.getState());
        assertEquals("log-1", p.getLogId());
        assertEquals("同步", p.getJobName());
        assertEquals("运行中", p.getStateLabel());
    }

    @Test
    void start_shouldIgnoreJobWithoutId() {
        registry.start(new SysJob(), "log-1");
        registry.start(null, "log-1");

        assertTrue(registry.listAll().isEmpty());
    }

    @Test
    void update_shouldKeepExplicitPercent() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.update("job-1", 40, "已处理 4 批");

        JobProgress p = registry.get("job-1");
        assertEquals(40, p.getPercent());
        assertEquals("已处理 4 批", p.getMessage());
    }

    @Test
    void update_shouldComputePercentFromCounters() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.update("job-1", 3L, 10L, "已同步 3/10");

        JobProgress p = registry.get("job-1");
        assertEquals(30, p.getPercent());
        assertEquals(3L, p.getCurrent());
        assertEquals(10L, p.getTotal());
    }

    @Test
    void update_shouldCapPercentAt100() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.update("job-1", 12L, 10L, null);
        registry.update("job-1", 150, null);

        assertEquals(100, registry.get("job-1").getPercent());
    }

    @Test
    void update_shouldIgnoreZeroTotal() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.update("job-1", 0L, 0L, "空数据");

        JobProgress p = registry.get("job-1");
        assertNull(p.getPercent());
        assertEquals("空数据", p.getMessage());
    }

    @Test
    void update_shouldIgnoreUnknownJob() {
        registry.update("no-such-job", 10, "x");
        assertTrue(registry.listAll().isEmpty());
    }

    @Test
    void update_shouldIgnoreFinishedRun() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.success("job-1", "OK");
        registry.update("job-1", 10, "迟到的进度");

        JobProgress p = registry.get("job-1");
        assertEquals(JobRunState.SUCCESS, p.getState());
        assertNull(p.getMessage());
        assertEquals(100, p.getPercent());
    }

    @Test
    void success_shouldMarkFinishedWithRunTime() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.success("job-1", "OK");

        JobProgress p = registry.get("job-1");
        assertEquals(JobRunState.SUCCESS, p.getState());
        assertEquals("OK", p.getResult());
        assertEquals(100, p.getPercent());
        assertNotNull(p.getEndTime());
        assertNotNull(p.getRunTime());
        assertFalse(p.isRunning());
    }

    @Test
    void fail_shouldMarkFailed() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.fail("job-1", "异常boom");

        JobProgress p = registry.get("job-1");
        assertEquals(JobRunState.FAILED, p.getState());
        assertEquals("异常boom", p.getResult());
        assertNotNull(p.getEndTime());
    }

    @Test
    void failIfRunning_shouldNotOverrideSuccess() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.success("job-1", "OK");
        registry.failIfRunning("job-1", "兜底失败");

        assertEquals(JobRunState.SUCCESS, registry.get("job-1").getState());
    }

    @Test
    void failIfRunning_shouldMarkRunningAsFailed() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.failIfRunning("job-1", "任务异常终止");

        JobProgress p = registry.get("job-1");
        assertEquals(JobRunState.FAILED, p.getState());
        assertEquals("任务异常终止", p.getResult());
    }

    @Test
    void listAll_shouldSortByBeginTimeDesc() throws Exception {
        registry.start(job("job-1", "A"), "log-1");
        Thread.sleep(5);
        registry.start(job("job-2", "B"), "log-2");

        List<JobProgress> list = registry.listAll();
        assertEquals(2, list.size());
        assertEquals("job-2", list.get(0).getJobId());
        assertEquals("job-1", list.get(1).getJobId());
    }

    @Test
    void start_shouldReplacePreviousSnapshot() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.success("job-1", "OK");
        registry.start(job("job-1", "同步"), "log-2");

        JobProgress p = registry.get("job-1");
        assertEquals(JobRunState.RUNNING, p.getState());
        assertEquals("log-2", p.getLogId());
        assertNull(p.getResult());
        assertNull(p.getEndTime());
    }

    @Test
    void get_shouldReturnNullForUnknownJob() {
        assertNull(registry.get("no-such-job"));
        assertNull(registry.get(null));
    }

    @Test
    void remove_shouldDropSnapshot() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.remove("job-1");
        registry.remove(null);

        assertNull(registry.get("job-1"));
    }

    @Test
    void sweep_shouldDropExpiredFinishedSnapshot() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.success("job-1", "OK");
        registry.get("job-1").setEndTime(java.time.LocalDateTime.now().minusHours(2));

        assertNull(registry.get("job-1"));
        assertTrue(registry.listAll().isEmpty());
    }

    @Test
    void sweep_shouldKeepRunningSnapshot() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.get("job-1").setBeginTime(java.time.LocalDateTime.now().minusHours(2));

        assertNotNull(registry.get("job-1"));
    }

    @Test
    void getByLogId_shouldFindRunningSnapshot() {
        registry.start(job("job-1", "同步"), "log-1");
        registry.start(job("job-2", "部署"), "log-2");

        assertEquals("job-2", registry.getByLogId("log-2").getJobId());
        assertNull(registry.getByLogId("no-such-log"));
        assertNull(registry.getByLogId(null));
    }
}
