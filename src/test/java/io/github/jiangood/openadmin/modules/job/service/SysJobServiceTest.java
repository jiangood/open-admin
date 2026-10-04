package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.data.BaseRepository;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.example.HelloWorldJob;
import io.github.jiangood.openadmin.modules.job.quartz.QuartzManager;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysJobServiceTest {

    @Mock
    private BaseRepository<SysJob, String> repository;

    @Mock
    private QuartzManager quartzService;

    @Mock
    private SysJobLogRepository sysJobLogRepository;

    private SysJobService service;

    @BeforeEach
    void setUp() {
        service = new SysJobService(quartzService, sysJobLogRepository);
        ReflectionTestUtils.setField(service, "repository", repository);
    }

    private SysJob newJob(String cron, String jobClass, boolean enabled) {
        SysJob job = new SysJob();
        job.setName("job-1");
        job.setCron(cron);
        job.setJobClass(jobClass);
        job.setEnabled(enabled);
        return job;
    }

    @Test
    void save_enabledJobInvalidCron_throwsBeforeTouchingQuartz() throws Exception {
        SysJob job = newJob("not-a-cron", HelloWorldJob.class.getName(), true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> service.save(job, null));
        verify(quartzService, never()).deleteJobByName(any());
        verify(quartzService, never()).deleteJob(any());
        verify(quartzService, never()).scheduleJob(any());
    }

    @Test
    void save_enabledJobInvalidJobClass_throwsBeforeTouchingQuartz() throws Exception {
        SysJob job = newJob("0 */5 * * * ?", "com.example.NoSuchJob", true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> service.save(job, null));
        verify(quartzService, never()).deleteJobByName(any());
        verify(quartzService, never()).deleteJob(any());
        verify(quartzService, never()).scheduleJob(any());
    }

    @Test
    void save_enabledJobValidParams_schedules() throws Exception {
        SysJob job = newJob("0 */5 * * * ?", HelloWorldJob.class.getName(), true);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.save(job, null);

        verify(quartzService).deleteJob(any());
        verify(quartzService).scheduleJob(any());
    }

    @Test
    void save_disabledJobInvalidCron_skipsValidation() throws Exception {
        SysJob job = newJob("not-a-cron", HelloWorldJob.class.getName(), false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.save(job, null);

        verify(quartzService).deleteJob(any());
        verify(quartzService, never()).scheduleJob(any());
    }
}