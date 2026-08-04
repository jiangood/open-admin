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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
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

    private Page<SysFile> emptyPage() {
        return new PageImpl<>(List.of());
    }

    private Page<SysFile> pageOf(SysFile... files) {
        return new PageImpl<>(List.of(files));
    }

    @Test
    void execute_shouldMarkUnclaimedThenDeletePending() throws Exception {
        SysFile a = new SysFile("id-a");
        a.setObjectName("public/202607/id-a.jpg");
        SysFile b = new SysFile("id-b");
        b.setObjectName("public/202607/id-b.jpg");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.updateStatusByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), eq(FileStatus.PENDING_DELETE), any(Date.class))).thenReturn(2);
        when(sysFileRepository.findByStatus(eq(FileStatus.IN_USE), any(Pageable.class))).thenReturn(emptyPage());
        when(sysFileRepository.findByStatus(eq(FileStatus.PENDING_DELETE), any(Pageable.class))).thenReturn(pageOf(a, b), emptyPage());
        when(sysFileService.deletePhysicalFile(a)).thenReturn(true);
        when(sysFileService.deletePhysicalFile(b)).thenReturn(false);

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository).updateStatusByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), eq(FileStatus.PENDING_DELETE), any(Date.class));
        verify(sysFileService).deletePhysicalFile(a);
        verify(sysFileService).deletePhysicalFile(b);
        verify(sysFileRepository).deleteAllByIdInBatch(List.of("id-a"));
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
        when(sysFileRepository.updateStatusByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), eq(FileStatus.PENDING_DELETE), any(Date.class))).thenReturn(0);
        when(sysFileRepository.findByStatus(eq(FileStatus.IN_USE), any(Pageable.class))).thenReturn(pageOf(claimed), emptyPage());
        when(jdbcRunner.existsById("sys_article", "article-999")).thenReturn(false);
        when(sysFileRepository.findByStatus(eq(FileStatus.PENDING_DELETE), any(Pageable.class))).thenReturn(pageOf(claimed), emptyPage());
        when(sysFileService.deletePhysicalFile(claimed)).thenReturn(true);

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository).updateStatusByObjectNames(List.of("public/202607/id-c.jpg"), FileStatus.PENDING_DELETE);
        verify(sysFileRepository).deleteAllByIdInBatch(List.of("id-c"));
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
        when(sysFileRepository.updateStatusByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), eq(FileStatus.PENDING_DELETE), any(Date.class))).thenReturn(0);
        when(sysFileRepository.findByStatus(eq(FileStatus.IN_USE), any(Pageable.class))).thenReturn(pageOf(claimed), emptyPage());
        when(jdbcRunner.existsById("sys_article", "article-1")).thenReturn(true);
        when(sysFileRepository.findByStatus(eq(FileStatus.PENDING_DELETE), any(Pageable.class))).thenReturn(emptyPage());

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository, never()).updateStatusByObjectNames(any(), any());
        verify(sysFileRepository, never()).deleteAllByIdInBatch(any());
        verify(sysFileService, never()).deletePhysicalFile(any());
        assertTrue(result.contains("标记孤儿 0 个"));
    }

    @Test
    void execute_shouldSkipWhenExistenceCheckFails() throws Exception {
        SysFile claimed = new SysFile("id-c");
        claimed.setObjectName("public/202607/id-c.jpg");
        claimed.setJoinTable("sys_article");
        claimed.setJoinId("article-999");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.updateStatusByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), eq(FileStatus.PENDING_DELETE), any(Date.class))).thenReturn(0);
        when(sysFileRepository.findByStatus(eq(FileStatus.IN_USE), any(Pageable.class))).thenReturn(pageOf(claimed), emptyPage());
        when(jdbcRunner.existsById("sys_article", "article-999")).thenThrow(new RuntimeException("table not found"));
        when(sysFileRepository.findByStatus(eq(FileStatus.PENDING_DELETE), any(Pageable.class))).thenReturn(emptyPage());

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository, never()).updateStatusByObjectNames(any(), any());
        verify(sysFileRepository, never()).deleteAllByIdInBatch(any());
        verify(sysFileService, never()).deletePhysicalFile(any());
        assertTrue(result.contains("标记孤儿 0 个"));
    }

    @Test
    void execute_shouldRetryFailedPendingDelete() throws Exception {
        SysFile file = new SysFile("id-d");
        file.setObjectName("public/202607/id-d.jpg");
        file.setStatus(FileStatus.PENDING_DELETE);

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.updateStatusByStatusAndCreateTimeBefore(eq(FileStatus.TEMP), eq(FileStatus.PENDING_DELETE), any(Date.class))).thenReturn(0);
        when(sysFileRepository.findByStatus(eq(FileStatus.IN_USE), any(Pageable.class))).thenReturn(emptyPage());
        when(sysFileRepository.findByStatus(eq(FileStatus.PENDING_DELETE), any(Pageable.class))).thenReturn(pageOf(file), emptyPage());
        when(sysFileService.deletePhysicalFile(file)).thenReturn(false);

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileService).deletePhysicalFile(file);
        verify(sysFileRepository, never()).deleteAllByIdInBatch(any());
        assertTrue(result.contains("删除待删 0 个"));
    }
}
