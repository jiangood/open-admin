package io.github.jiangood.openadmin.modules.system.job;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.data.JdbcRunner;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.repository.SysFileRepository;
import io.github.jiangood.openadmin.modules.system.service.SysFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleanTempFileJobTest {

    @Mock
    private SysFileRepository sysFileRepository;
    @Mock
    private SysFileService sysFileService;
    @Mock
    private JdbcRunner jdbcRunner;
    @Mock
    private SystemProperties systemProperties;

    private CleanTempFileJob job;

    @BeforeEach
    void setUp() {
        job = new CleanTempFileJob();
        ReflectionTestUtils.setField(job, "sysFileRepository", sysFileRepository);
        ReflectionTestUtils.setField(job, "sysFileService", sysFileService);
        ReflectionTestUtils.setField(job, "jdbcRunner", jdbcRunner);
        ReflectionTestUtils.setField(job, "systemProperties", systemProperties);
    }

    @Test
    void execute_shouldMarkUnclaimedThenDeletePending() throws Exception {
        SysFile a = new SysFile("id-a");
        a.setObjectName("public/202607/id-a.jpg");
        SysFile b = new SysFile("id-b");
        b.setObjectName("public/202607/id-b.jpg");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), any(Date.class))).thenReturn(List.of(a, b));
        when(sysFileRepository.findByStatus(FileStatus.IN_USE)).thenReturn(List.of());
        when(sysFileRepository.findByStatus(FileStatus.PENDING_DELETE)).thenReturn(List.of(a, b));
        when(sysFileService.deleteFileInternal(a)).thenReturn(true);
        when(sysFileService.deleteFileInternal(b)).thenReturn(false);

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository).updateStatusByObjectNames(List.of("public/202607/id-a.jpg", "public/202607/id-b.jpg"), FileStatus.PENDING_DELETE);
        verify(sysFileService).deleteFileInternal(a);
        verify(sysFileService).deleteFileInternal(b);
        assertTrue(result.contains("标记未认领 2 个"));
        assertTrue(result.contains("删除待删 1 个"));
    }

    @Test
    void execute_shouldMarkOrphanWhenRecordMissing() throws Exception {
        SysFile claimed = new SysFile("id-c");
        claimed.setObjectName("public/202607/id-c.jpg");
        claimed.setJoinTable("sys_article");
        claimed.setJoinId("article-999");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), any(Date.class))).thenReturn(List.of());
        when(sysFileRepository.findByStatus(FileStatus.IN_USE)).thenReturn(List.of(claimed));
        when(jdbcRunner.existsById("sys_article", "article-999")).thenReturn(false);
        when(sysFileRepository.findByStatus(FileStatus.PENDING_DELETE)).thenReturn(List.of(claimed));
        when(sysFileService.deleteFileInternal(claimed)).thenReturn(true);

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository).updateStatusByObjectNames(List.of("public/202607/id-c.jpg"), FileStatus.PENDING_DELETE);
        verify(sysFileService).deleteFileInternal(claimed);
        assertTrue(result.contains("标记孤儿 1 个"));
        assertTrue(result.contains("删除待删 1 个"));
    }

    @Test
    void execute_shouldKeepInUseFileWhenRecordExists() throws Exception {
        SysFile claimed = new SysFile("id-c");
        claimed.setObjectName("public/202607/id-c.jpg");
        claimed.setJoinTable("sys_article");
        claimed.setJoinId("article-1");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), any(Date.class))).thenReturn(List.of());
        when(sysFileRepository.findByStatus(FileStatus.IN_USE)).thenReturn(List.of(claimed));
        when(jdbcRunner.existsById("sys_article", "article-1")).thenReturn(true);
        when(sysFileRepository.findByStatus(FileStatus.PENDING_DELETE)).thenReturn(List.of());

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository, never()).updateStatusByObjectNames(any(), any());
        verify(sysFileService, never()).deleteFileInternal(any());
        assertTrue(result.contains("标记孤儿 0 个"));
    }

    @Test
    void execute_shouldSkipWhenExistenceCheckFails() throws Exception {
        SysFile claimed = new SysFile("id-c");
        claimed.setObjectName("public/202607/id-c.jpg");
        claimed.setJoinTable("sys_article");
        claimed.setJoinId("article-999");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), any(Date.class))).thenReturn(List.of());
        when(sysFileRepository.findByStatus(FileStatus.IN_USE)).thenReturn(List.of(claimed));
        when(jdbcRunner.existsById("sys_article", "article-999")).thenThrow(new RuntimeException("table not found"));
        when(sysFileRepository.findByStatus(FileStatus.PENDING_DELETE)).thenReturn(List.of());

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository, never()).updateStatusByObjectNames(any(), any());
        verify(sysFileService, never()).deleteFileInternal(any());
        assertTrue(result.contains("标记孤儿 0 个"));
    }

    @Test
    void execute_shouldRetryFailedPendingDelete() throws Exception {
        SysFile file = new SysFile("id-d");
        file.setObjectName("public/202607/id-d.jpg");
        file.setStatus(FileStatus.PENDING_DELETE);

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), any(Date.class))).thenReturn(List.of());
        when(sysFileRepository.findByStatus(FileStatus.IN_USE)).thenReturn(List.of());
        when(sysFileRepository.findByStatus(FileStatus.PENDING_DELETE)).thenReturn(List.of(file));
        when(sysFileService.deleteFileInternal(file)).thenReturn(false);

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileService).deleteFileInternal(file);
        assertTrue(result.contains("删除待删 0 个"));
    }
}
