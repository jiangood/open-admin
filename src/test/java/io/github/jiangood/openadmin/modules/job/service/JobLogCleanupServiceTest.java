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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobLogCleanupServiceTest {

    @Mock
    private SystemProperties systemProperties;
    @Mock
    private SysJobLogRepository sysJobLogRepository;
    @Mock
    private FileLogService fileLogService;
    @Mock
    private JobLogSizeService jobLogSizeService;

    private TestService service;

    @BeforeEach
    void setUp() {
        service = new TestService();
    }

    /**
     * 测试用子类：当前日期可手动推进，异步提交可在当前线程立即执行或暂存（模拟清理进行中）
     */
    private class TestService extends JobLogCleanupService {

        private String day = "2026-01-01";
        private final List<Runnable> pending = new ArrayList<>();
        private boolean runDirectly = true;

        TestService() {
            super(systemProperties, sysJobLogRepository, fileLogService, jobLogSizeService);
        }

        void advanceDay() {
            day = "2026-01-02";
        }

        void runDirectly() {
            runDirectly = true;
        }

        void holdPending() {
            runDirectly = false;
        }

        void drainPending() {
            List<Runnable> copy = new ArrayList<>(pending);
            pending.clear();
            copy.forEach(Runnable::run);
        }

        boolean hasPending() {
            return !pending.isEmpty();
        }

        @Override
        String currentDay() {
            return day;
        }

        @Override
        void runAsync(Runnable task) {
            if (runDirectly) {
                task.run();
            } else {
                pending.add(task);
            }
        }
    }

    private SysJobLog log(String id) {
        SysJobLog log = new SysJobLog();
        log.setId(id);
        return log;
    }

    @Test
    void consider_shouldSkipWhenRetainDaysDisabled() {
        when(systemProperties.getJobLogRetainDays()).thenReturn(0);

        service.considerCleanup();

        verifyNoInteractions(sysJobLogRepository);
    }

    @Test
    void consider_shouldSkipWhenRetainDaysNegative() {
        when(systemProperties.getJobLogRetainDays()).thenReturn(-1);

        service.considerCleanup();

        verifyNoInteractions(sysJobLogRepository);
    }

    @Test
    void consider_shouldCleanExpiredRecordsAndFiles() throws Exception {
        when(systemProperties.getJobLogRetainDays()).thenReturn(30);
        SysJobLog a = log("log-a");
        SysJobLog b = log("log-b");
        when(sysJobLogRepository.findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a, b)), new PageImpl<>(List.of()));

        service.considerCleanup();

        verify(sysJobLogRepository).deleteAll(List.of(a, b));
        verify(fileLogService).deleteLog("job/log-a");
        verify(fileLogService).deleteLog("job/log-b");
        verify(jobLogSizeService).invalidate();
    }

    @Test
    void consider_shouldRunOncePerDay() {
        when(systemProperties.getJobLogRetainDays()).thenReturn(30);
        when(sysJobLogRepository.findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // 同一自然日多次触发只实际执行一次清理
        service.considerCleanup();
        service.considerCleanup();
        service.considerCleanup();

        verify(sysJobLogRepository, times(1))
                .findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class));

        // 跨到新的一天后可再次执行
        service.advanceDay();
        service.considerCleanup();

        verify(sysJobLogRepository, times(2))
                .findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void consider_shouldSkipWhileCleanupIsRunning() {
        when(systemProperties.getJobLogRetainDays()).thenReturn(30);

        // 第一次触发暂存不执行（模拟清理排队/进行中），防重入标记被占用
        service.holdPending();
        service.considerCleanup();
        verifyNoInteractions(sysJobLogRepository);

        // 第二次触发（已跨天，不被节流拦截）应被防重入拦下，且不占用当日额度
        service.runDirectly();
        service.advanceDay();
        service.considerCleanup();
        verify(sysJobLogRepository, never()).findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class));

        // 完成暂存的清理后防重入释放，任务执行正常完成
        service.drainPending();
        verify(sysJobLogRepository, times(1)).findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void consider_shouldContinueWhenFileDeleteFails() throws Exception {
        when(systemProperties.getJobLogRetainDays()).thenReturn(30);
        SysJobLog a = log("log-a");
        when(sysJobLogRepository.findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a)), new PageImpl<>(List.of()));
        doThrow(new IOException("io")).when(fileLogService).deleteLog("job/log-a");

        service.considerCleanup();

        verify(sysJobLogRepository).deleteAll(List.of(a));
        verify(fileLogService).deleteLog("job/log-a");
    }

    @Test
    void consider_shouldDoNothingWhenNoExpiredRecords() {
        when(systemProperties.getJobLogRetainDays()).thenReturn(30);
        when(sysJobLogRepository.findByUpdateTimeBefore(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.considerCleanup();

        verify(sysJobLogRepository, never()).deleteAll(any());
    }
}
