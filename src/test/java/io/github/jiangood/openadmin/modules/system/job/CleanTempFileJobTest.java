package io.github.jiangood.openadmin.modules.system.job;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.data.JdbcRunner;
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
    void execute_shouldBatchDeleteOnlyPhysicallyRemovedFiles() throws Exception {
        SysFile a = new SysFile("id-a");
        a.setObjectName("public/202607/id-a.jpg");
        SysFile b = new SysFile("id-b");
        b.setObjectName("public/202607/id-b.jpg");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findUnclaimedFiles(any(Date.class))).thenReturn(List.of(a, b));
        when(sysFileService.deletePhysicalFile(a)).thenReturn(true);
        when(sysFileService.deletePhysicalFile(b)).thenReturn(false);

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileService).deletePhysicalFile(a);
        verify(sysFileService).deletePhysicalFile(b);
        verify(sysFileRepository).deleteAllInBatch(List.of(a));
        assertTrue(result.contains("删除未认领 1 个"));
    }

    @Test
    void execute_shouldSkipBatchDeleteWhenNothingRemoved() throws Exception {
        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findUnclaimedFiles(any(Date.class))).thenReturn(List.of());

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository, never()).deleteAllInBatch(any());
        assertTrue(result.contains("删除未认领 0 个"));
    }

    @Test
    void execute_shouldDeleteOrphanClaimedFiles() throws Exception {
        SysFile claimed = new SysFile("id-c");
        claimed.setObjectName("public/202607/id-c.jpg");
        claimed.setJoinTable("sys_article");
        claimed.setJoinId("article-999");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findUnclaimedFiles(any(Date.class))).thenReturn(List.of());
        when(sysFileRepository.findClaimedFiles()).thenReturn(List.of(claimed));
        when(jdbcRunner.existsById("sys_article", "article-999")).thenReturn(false);
        when(sysFileService.deletePhysicalFile(claimed)).thenReturn(true);

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository).deleteAllInBatch(List.of(claimed));
        assertTrue(result.contains("孤儿文件 1 个"));
    }

    @Test
    void execute_shouldKeepClaimedFileWhenRecordExists() throws Exception {
        SysFile claimed = new SysFile("id-c");
        claimed.setObjectName("public/202607/id-c.jpg");
        claimed.setJoinTable("sys_article");
        claimed.setJoinId("article-1");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findUnclaimedFiles(any(Date.class))).thenReturn(List.of());
        when(sysFileRepository.findClaimedFiles()).thenReturn(List.of(claimed));
        when(jdbcRunner.existsById("sys_article", "article-1")).thenReturn(true);

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository, never()).deleteAllInBatch(any());
        verify(sysFileService, never()).deletePhysicalFile(any());
        assertTrue(result.contains("孤儿文件 0 个"));
    }

    @Test
    void execute_shouldSkipWhenExistenceCheckFails() throws Exception {
        SysFile claimed = new SysFile("id-c");
        claimed.setObjectName("public/202607/id-c.jpg");
        claimed.setJoinTable("sys_article");
        claimed.setJoinId("article-999");

        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());
        when(sysFileRepository.findUnclaimedFiles(any(Date.class))).thenReturn(List.of());
        when(sysFileRepository.findClaimedFiles()).thenReturn(List.of(claimed));
        when(jdbcRunner.existsById("sys_article", "article-999")).thenThrow(new RuntimeException("table not found"));

        String result = job.execute(new JobDataMap(), mock(Logger.class));

        verify(sysFileRepository, never()).deleteAllInBatch(any());
        verify(sysFileService, never()).deletePhysicalFile(any());
        assertTrue(result.contains("孤儿文件 0 个"));
    }
}
