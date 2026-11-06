package io.github.jiangood.openadmin.modules.job.quartz;

import io.github.jiangood.openadmin.modules.job.JobDefinition;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.modules.job.JobRegistrar;
import io.github.jiangood.openadmin.modules.job.entity.JobLogAggregation;
import io.github.jiangood.openadmin.modules.job.entity.SysJob;
import io.github.jiangood.openadmin.modules.job.repository.SysJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobRegistrationManagerTest {

    @Mock
    private SysJobRepository sysJobRepository;

    @Mock
    private JobDefinitionScanner jobDefinitionScanner;

    private final List<JobRegistrar> registrars = new ArrayList<>();

    private JobRegistrationManager manager;

    @BeforeEach
    void setUp() {
        manager = new JobRegistrationManager(sysJobRepository, jobDefinitionScanner, registrars);
        lenient().when(sysJobRepository.findByJobKey(anyString())).thenReturn(null);
        lenient().when(sysJobRepository.findByJobClass(anyString())).thenReturn(List.of());
        lenient().when(sysJobRepository.findByName(anyString())).thenReturn(null);
        lenient().when(sysJobRepository.findAll()).thenReturn(List.of());
    }

    @Test
    void registersMissingJobAsDisabledByDefault() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class));

        JobRegistrationManager.SyncResult result = manager.sync();

        SysJob saved = captureSaved();
        assertEquals(AutoDisabledJob.class.getName(), saved.getJobKey());
        assertEquals("自动任务", saved.getName());
        assertEquals(AutoDisabledJob.class.getName(), saved.getJobClass());
        assertEquals("0 0 3 * * ?", saved.getCron());
        assertFalse(saved.getEnabled());
        assertEquals(JobLogAggregation.PER_RUN, saved.getLogAggregation());
        assertEquals(1, result.scanned());
        assertEquals(1, result.registered());
    }

    @Test
    void declaredKeyIsUsedInsteadOfClassName() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(StableKeyJob.class));

        manager.sync();

        assertEquals("stable.key", captureSaved().getJobKey());
    }

    @Test
    void defaultEnabledJobIsRegisteredEnabled() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoEnabledJob.class));

        manager.sync();

        SysJob saved = captureSaved();
        assertTrue(saved.getEnabled());
        assertEquals("0 0 3 * * ?", saved.getCron());
    }

    @Test
    void defaultEnabledWithoutCronIsRegisteredDisabled() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoNoCronJob.class));

        manager.sync();

        SysJob saved = captureSaved();
        assertFalse(saved.getEnabled());
        assertNull(saved.getCron());
    }

    @Test
    void unannotatedBaseJobIsRegisteredDisabled() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoPlainBaseJob.class));

        manager.sync();

        SysJob saved = captureSaved();
        assertEquals("AutoPlainBaseJob", saved.getName());
        assertNull(saved.getCron());
        assertFalse(saved.getEnabled());
    }

    @Test
    void existingKeyIsNotDuplicatedAfterClassRename() {
        SysJob existing = new SysJob();
        existing.setJobKey("stable.key");
        existing.setJobClass("com.example.legacy.OldNameJob");
        when(jobDefinitionScanner.scan()).thenReturn(List.of(StableKeyJob.class));
        when(sysJobRepository.findByJobKey("stable.key")).thenReturn(existing);

        manager.sync();

        verify(sysJobRepository, never()).save(any());
    }

    @Test
    void legacyRecordWithoutKeyIsBackfilledByJobClass() {
        SysJob existing = new SysJob();
        existing.setId("1");
        existing.setName("旧任务");
        existing.setJobClass(AutoDisabledJob.class.getName());
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class));
        when(sysJobRepository.findAll()).thenReturn(List.of(existing));
        when(sysJobRepository.findByJobClass(AutoDisabledJob.class.getName())).thenReturn(List.of(existing));

        manager.sync();

        assertEquals(AutoDisabledJob.class.getName(), existing.getJobKey());
        verify(sysJobRepository, times(1)).save(existing);
    }

    @Test
    void movedClassIsNotDuplicated() {
        SysJob existing = new SysJob();
        existing.setId("9");
        existing.setName("换包任务");
        existing.setJobClass("com.legacy.MovedJob");
        existing.setJobKey("com.legacy.MovedJob");
        when(jobDefinitionScanner.scan()).thenReturn(List.of(MovedJob.class));
        when(sysJobRepository.findAll()).thenReturn(List.of(existing));

        manager.sync();

        verify(sysJobRepository, never()).save(any());
    }

    @Test
    void movedClassWithCustomKeyIsRegisteredAsNew() {
        SysJob existing = new SysJob();
        existing.setId("9");
        existing.setName("换包任务");
        existing.setJobClass("com.legacy.MovedJob");
        existing.setJobKey("legacy.custom");
        when(jobDefinitionScanner.scan()).thenReturn(List.of(MovedJob.class));
        when(sysJobRepository.findAll()).thenReturn(List.of(existing));

        manager.sync();

        assertEquals(MovedJob.class.getName(), captureSaved().getJobClass());
    }

    @Test
    void duplicateKeysOnlyRegisterFirstByClassName() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(DuplicateBJob.class, DuplicateAJob.class));

        manager.sync();

        SysJob saved = captureSaved();
        assertEquals(DuplicateAJob.class.getName(), saved.getJobClass());
        assertEquals("duplicate.key", saved.getJobKey());
    }

    @Test
    void spiOverridesAnnotationDefaults() {
        registrars.add(() -> List.of(JobDefinition.builder()
                .jobClass(AutoDisabledJob.class)
                .name("覆盖后的名称")
                .cron("0 0 5 * * ?")
                .enabled(true)
                .logAggregation(JobLogAggregation.DAILY)
                .build()));
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class));

        manager.sync();

        SysJob saved = captureSaved();
        assertEquals(AutoDisabledJob.class.getName(), saved.getJobKey());
        assertEquals("覆盖后的名称", saved.getName());
        assertEquals("0 0 5 * * ?", saved.getCron());
        assertTrue(saved.getEnabled());
        assertEquals(JobLogAggregation.DAILY, saved.getLogAggregation());
    }

    @Test
    void spiCanRegisterJobNotReturnedByScanner() {
        registrars.add(() -> List.of(JobDefinition.builder()
                .jobClass(SpiOnlyJob.class)
                .name("SPI 注册任务")
                .cron("0 0 6 * * ?")
                .enabled(false)
                .build()));
        when(jobDefinitionScanner.scan()).thenReturn(List.of());

        manager.sync();

        SysJob saved = captureSaved();
        assertEquals(SpiOnlyJob.class.getName(), saved.getJobKey());
        assertEquals("SPI 注册任务", saved.getName());
        assertEquals("0 0 6 * * ?", saved.getCron());
        assertFalse(saved.getEnabled());
    }

    @Test
    void spiOverridesAutoRegisterFalse() {
        registrars.add(() -> List.of(JobDefinition.builder()
                .jobClass(AutoOptOutJob.class)
                .name("SPI 强制注册")
                .build()));
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoOptOutJob.class));

        manager.sync();

        assertEquals("SPI 强制注册", captureSaved().getName());
    }

    @Test
    void optOutJobIsSkipped() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoOptOutJob.class));

        manager.sync();

        verify(sysJobRepository, never()).save(any());
    }

    @Test
    void classWithoutJobSuffixIsSkipped() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(NotSuffixedTask.class));

        manager.sync();

        verify(sysJobRepository, never()).save(any());
    }

    @Test
    void nameConflictAppendsSimpleClassName() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class));
        when(sysJobRepository.findByName("自动任务")).thenReturn(new SysJob());
        when(sysJobRepository.findByName("自动任务（AutoDisabledJob）")).thenReturn(null);

        manager.sync();

        assertEquals("自动任务（AutoDisabledJob）", captureSaved().getName());
    }

    @Test
    void findUnregisteredReturnsOnlyMissingJobs() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class, AutoEnabledJob.class));
        SysJob existing = new SysJob();
        existing.setJobKey(AutoDisabledJob.class.getName());
        when(sysJobRepository.findByJobKey(AutoDisabledJob.class.getName())).thenReturn(existing);

        List<JobDefinition> unregistered = manager.findUnregistered();

        assertEquals(1, unregistered.size());
        assertEquals(AutoEnabledJob.class.getName(), unregistered.get(0).getKey());
    }

    @Test
    void importOnlyRegistersSelectedKeys() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class, AutoEnabledJob.class));

        int count = manager.importJobs(List.of(AutoEnabledJob.class.getName()));

        assertEquals(1, count);
        assertEquals(AutoEnabledJob.class.getName(), captureSaved().getJobClass());
    }

    @Test
    void importAllWhenKeysAreEmpty() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class, AutoEnabledJob.class));

        int count = manager.importJobs(List.of());

        assertEquals(2, count);
    }

    @Test
    void duplicateInsertFromAnotherInstanceDoesNotBreakStartup() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class));
        when(sysJobRepository.save(any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> manager.sync());
    }

    private SysJob captureSaved() {
        ArgumentCaptor<SysJob> captor = ArgumentCaptor.forClass(SysJob.class);
        verify(sysJobRepository).save(captor.capture());
        return captor.getValue();
    }

    @JobDescription(label = "自动任务", defaultCron = "0 0 3 * * ?")
    static class AutoDisabledJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "自动启用任务", defaultCron = "0 0 3 * * ?", defaultEnabled = true)
    static class AutoEnabledJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "无 cron 任务", defaultEnabled = true)
    static class AutoNoCronJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "稳定 key 任务", key = "stable.key")
    static class StableKeyJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "重复 key A", key = "duplicate.key")
    static class DuplicateAJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "重复 key B", key = "duplicate.key")
    static class DuplicateBJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "不自动注册", autoRegister = false)
    static class AutoOptOutJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    static class SpiOnlyJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "换包任务")
    static class MovedJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "类名不以 Job 结尾")
    static class NotSuffixedTask implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    static class AutoPlainBaseJob extends io.github.jiangood.openadmin.modules.job.BaseJob {
        @Override
        public String execute(org.quartz.JobDataMap data, org.slf4j.Logger logger) {
            return null;
        }
    }
}
