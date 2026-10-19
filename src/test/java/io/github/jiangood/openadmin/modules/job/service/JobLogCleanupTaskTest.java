package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.entity.SysJobLog;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.logviewer.service.FileLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobLogCleanupTaskTest {

    @Mock
    private SystemProperties systemProperties;
    @Mock
    private SysJobLogRepository sysJobLogRepository;
    @Mock
    private FileLogService fileLogService;

    private JobLogCleanupTask task;

    @BeforeEach
    void setUp() {
        task = new JobLogCleanupTask(systemProperties, sysJobLogRepository, fileLogService);
    }

    private SysJobLog log(String id) {
        SysJobLog log = new SysJobLog();
        log.setId(id);
        return log;
    }

    @Test
    void clean_shouldSkipWhenRetainDaysDisabled() {
        when(systemProperties.getJobLogRetainDays()).thenReturn(0);

        task.cleanExpiredLogs();

        verifyNoInteractions(sysJobLogRepository);
    }

    @Test
    void clean_shouldSkipWhenRetainDaysNegative() {
        when(systemProperties.getJobLogRetainDays()).thenReturn(-1);

        task.cleanExpiredLogs();

        verifyNoInteractions(sysJobLogRepository);
    }

    @Test
    void clean_shouldDeleteExpiredRecordsAndFiles() throws Exception {
        when(systemProperties.getJobLogRetainDays()).thenReturn(30);
        SysJobLog a = log("log-a");
        SysJobLog b = log("log-b");
        when(sysJobLogRepository.findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a, b)), new PageImpl<>(List.of()));

        task.cleanExpiredLogs();

        verify(sysJobLogRepository).deleteAll(List.of(a, b));
        verify(fileLogService).deleteLog("job/log-a");
        verify(fileLogService).deleteLog("job/log-b");
    }

    @Test
    void clean_shouldContinueWhenFileDeleteFails() throws Exception {
        when(systemProperties.getJobLogRetainDays()).thenReturn(30);
        SysJobLog a = log("log-a");
        when(sysJobLogRepository.findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a)), new PageImpl<>(List.of()));
        doThrow(new IOException("io")).when(fileLogService).deleteLog("job/log-a");

        task.cleanExpiredLogs();

        verify(sysJobLogRepository).deleteAll(List.of(a));
        verify(fileLogService).deleteLog("job/log-a");
    }

    @Test
    void clean_shouldDoNothingWhenNoExpiredRecords() {
        when(systemProperties.getJobLogRetainDays()).thenReturn(30);
        when(sysJobLogRepository.findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        task.cleanExpiredLogs();

        verify(sysJobLogRepository, never()).deleteAll(any());
    }
}
