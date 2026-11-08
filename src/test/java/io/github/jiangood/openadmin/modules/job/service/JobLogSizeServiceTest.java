package io.github.jiangood.openadmin.modules.job.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.modules.job.dto.LogSizeStat;
import io.github.jiangood.openadmin.modules.job.repository.SysJobLogRepository;
import io.github.jiangood.openadmin.modules.logviewer.config.FileLogConfig;
import io.github.jiangood.openadmin.modules.logviewer.service.FileLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobLogSizeServiceTest {

    @TempDir
    Path logDir;

    @Mock
    private SysJobLogRepository sysJobLogRepository;

    @Mock
    private SystemProperties systemProperties;

    private JobLogSizeService service;

    @BeforeEach
    void setUp() throws Exception {
        FileLogConfig config = new FileLogConfig();
        Field logPath = FileLogConfig.class.getDeclaredField("logPath");
        logPath.setAccessible(true);
        logPath.set(config, logDir.toString());

        FileLogService fileLogService = new FileLogService();
        Field field = FileLogService.class.getDeclaredField("fileLogConfig");
        field.setAccessible(true);
        field.set(fileLogService, config);

        service = new JobLogSizeService(fileLogService, sysJobLogRepository, systemProperties);
    }

    private void writeLog(String name, int bytes) throws Exception {
        Files.createDirectories(logDir.resolve("job"));
        Files.write(logDir.resolve("job").resolve(name), new byte[bytes]);
    }

    @Test
    void total_shouldSumLogFilesOnly() throws Exception {
        writeLog("a.log", 100);
        writeLog("b.log", 200);
        Files.write(logDir.resolve("job").resolve("ignore.txt"), new byte[50]);
        Files.createDirectories(logDir.resolve("job").resolve("nested"));
        Files.write(logDir.resolve("job").resolve("nested").resolve("c.log"), new byte[999]);

        LogSizeStat stat = service.total();

        assertEquals(300, stat.getBytes());
        assertEquals(2, stat.getFiles());
        assertNull(stat.getWarnMb());
        assertNull(stat.getWarn());
    }

    @Test
    void total_shouldNotCreateDirectoryWhenMissing() {
        LogSizeStat stat = service.total();

        assertEquals(0, stat.getBytes());
        assertEquals(0, stat.getFiles());
        assertFalse(Files.exists(logDir.resolve("job")));
    }

    @Test
    void total_shouldMarkWarnWhenOverThreshold() throws Exception {
        writeLog("a.log", 2 * 1024 * 1024);
        when(systemProperties.getJobLogSizeWarnMb()).thenReturn(1);

        LogSizeStat stat = service.total();

        assertEquals(1, stat.getWarnMb());
        assertTrue(stat.getWarn());
    }

    @Test
    void total_shouldCacheUntilInvalidated() throws Exception {
        writeLog("a.log", 100);
        assertEquals(100, service.total().getBytes());

        writeLog("b.log", 200);
        assertEquals(100, service.total().getBytes(), "缓存期内不应重新扫描");

        service.invalidate();
        assertEquals(300, service.total().getBytes());
    }

    @Test
    void forJob_shouldSumFilesOfJobLogs() throws Exception {
        writeLog("log-a.log", 100);
        writeLog("log-b.log", 200);
        writeLog("log-other.log", 400);
        when(sysJobLogRepository.findIdsByJobId("job-1")).thenReturn(List.of("log-a", "log-b", "log-missing"));

        LogSizeStat stat = service.forJob("job-1");

        assertEquals(300, stat.getBytes());
        assertEquals(2, stat.getFiles());
    }
}
