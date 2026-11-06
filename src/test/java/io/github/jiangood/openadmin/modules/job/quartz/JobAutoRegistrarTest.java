package io.github.jiangood.openadmin.modules.job.quartz;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
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
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobAutoRegistrarTest {

    @Mock
    private SysJobRepository sysJobRepository;

    @Mock
    private SystemProperties systemProperties;

    @Mock
    private JobDefinitionScanner jobDefinitionScanner;

    private JobAutoRegistrar registrar;

    @BeforeEach
    void setUp() {
        registrar = new JobAutoRegistrar(sysJobRepository, systemProperties, jobDefinitionScanner);
        lenient().when(systemProperties.isJobEnable()).thenReturn(true);
        lenient().when(systemProperties.isJobAutoRegister()).thenReturn(true);
    }

    @Test
    void registersMissingJobAsDisabledByDefault() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class));

        registrar.run();

        SysJob saved = captureSaved();
        assertEquals("自动任务", saved.getName());
        assertEquals(AutoDisabledJob.class.getName(), saved.getJobClass());
        assertEquals("0 0 3 * * ?", saved.getCron());
        assertFalse(saved.getEnabled());
        assertEquals(JobLogAggregation.PER_RUN, saved.getLogAggregation());
    }

    @Test
    void defaultEnabledJobIsRegisteredEnabled() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoEnabledJob.class));

        registrar.run();

        SysJob saved = captureSaved();
        assertTrue(saved.getEnabled());
        assertEquals("0 0 3 * * ?", saved.getCron());
    }

    @Test
    void defaultEnabledWithoutCronIsRegisteredDisabled() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoNoCronJob.class));

        registrar.run();

        SysJob saved = captureSaved();
        assertFalse(saved.getEnabled());
        assertNull(saved.getCron());
    }

    @Test
    void unannotatedBaseJobIsRegisteredDisabled() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoPlainBaseJob.class));

        registrar.run();

        SysJob saved = captureSaved();
        assertEquals("AutoPlainBaseJob", saved.getName());
        assertNull(saved.getCron());
        assertFalse(saved.getEnabled());
    }

    @Test
    void existingJobClassIsNotDuplicated() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class));
        when(sysJobRepository.findByJobClass(AutoDisabledJob.class.getName()))
                .thenReturn(List.of(new SysJob()));

        registrar.run();

        verify(sysJobRepository, never()).save(any());
    }

    @Test
    void nameConflictAppendsSimpleClassName() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class));
        when(sysJobRepository.findByName("自动任务")).thenReturn(new SysJob());
        when(sysJobRepository.findByName("自动任务（AutoDisabledJob）")).thenReturn(null);

        registrar.run();

        assertEquals("自动任务（AutoDisabledJob）", captureSaved().getName());
    }

    @Test
    void optOutJobIsSkipped() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoOptOutJob.class));

        registrar.run();

        verify(sysJobRepository, never()).save(any());
    }

    @Test
    void classWithoutJobSuffixIsSkipped() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoTask.class));

        registrar.run();

        verify(sysJobRepository, never()).save(any());
    }

    @Test
    void duplicateInsertFromAnotherInstanceDoesNotBreakStartup() {
        when(jobDefinitionScanner.scan()).thenReturn(List.of(AutoDisabledJob.class));
        when(sysJobRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertDoesNotThrow(() -> registrar.run());
    }

    @Test
    void switchOffSkipsScan() {
        when(systemProperties.isJobAutoRegister()).thenReturn(false);

        registrar.run();

        verify(jobDefinitionScanner, never()).scan();
        verify(sysJobRepository, never()).save(any());
    }

    @Test
    void jobModuleDisabledSkipsScan() {
        when(systemProperties.isJobEnable()).thenReturn(false);

        registrar.run();

        verify(jobDefinitionScanner, never()).scan();
        verify(sysJobRepository, never()).save(any());
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

    @JobDescription(label = "不自动注册", autoRegister = false)
    static class AutoOptOutJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    @JobDescription(label = "类名不以 Job 结尾")
    static class AutoTask implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

    static class AutoPlainBaseJob extends BaseJob {
        @Override
        public String execute(JobDataMap data, Logger logger) {
            return null;
        }
    }
}
