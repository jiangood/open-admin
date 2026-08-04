package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.repository.SysFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysFileServiceTempTest {

    @Mock
    private SystemProperties systemProperties;
    @Mock
    private FileOperator fileOperator;
    @Mock
    private SysFileRepository sysFileRepository;
    @Mock
    private SysUserService sysUserService;

    private SysFileService sysFileService;

    private static final String JOIN_TABLE = "sys_article";
    private static final String JOIN_ID = "article-123";

    @BeforeEach
    void setUp() {
        sysFileService = new SysFileService(systemProperties, fileOperator, sysFileRepository, sysUserService);
    }

    @Test
    void claimList_shouldConfirmNewFiles() throws Exception {
        List<String> oldNames = List.of("public/202607/id-a.jpg");
        List<String> newNames = List.of("public/202607/id-a.jpg", "public/202607/id-b.jpg");

        sysFileService.claimList(JOIN_TABLE, JOIN_ID, oldNames, newNames);

        verify(sysFileRepository).updateJoinRefByObjectNames(JOIN_TABLE, JOIN_ID, List.of("public/202607/id-b.jpg"));
        verify(sysFileRepository, never()).findByObjectNameIn(any());
        verify(sysFileRepository, never()).updateStatusByObjectNames(any(), any());
    }

    @Test
    void claimList_shouldMarkRemovedFilesPendingDelete() throws Exception {
        List<String> oldNames = List.of("public/202607/id-a.jpg", "public/202607/id-b.jpg");
        List<String> newNames = List.of("public/202607/id-b.jpg");

        sysFileService.claimList(JOIN_TABLE, JOIN_ID, oldNames, newNames);

        verify(sysFileRepository, never()).updateJoinRefByObjectNames(any(), any(), any());
        verify(sysFileRepository).updateStatusByObjectNames(List.of("public/202607/id-a.jpg"), FileStatus.PENDING_DELETE);
        verify(sysFileRepository, never()).deleteAllInBatch(any());
        verify(fileOperator, never()).delete(any());
    }

    @Test
    void claimList_shouldHandleNullInputs() throws Exception {
        assertDoesNotThrow(() -> sysFileService.claimList(JOIN_TABLE, JOIN_ID, null, null));
        verify(sysFileRepository, never()).updateJoinRefByObjectNames(any(), any(), any());
        verify(sysFileRepository, never()).updateStatusByObjectNames(any(), any());
    }

    @Test
    void claimList_shouldHandleEmptyInputs() throws Exception {
        assertDoesNotThrow(() -> sysFileService.claimList(JOIN_TABLE, JOIN_ID, List.of(), List.of()));
        verify(sysFileRepository, never()).updateJoinRefByObjectNames(any(), any(), any());
        verify(sysFileRepository, never()).updateStatusByObjectNames(any(), any());
    }

    @Test
    void claimHtml_shouldExtractObjectNamesAndConfirm() throws Exception {
        String oldHtml = null;
        String newHtml = "<img src=\"/file/public/202607/550e8400-e29b-41d4-a716-446655440000.jpg\">";

        sysFileService.claimHtml(JOIN_TABLE, JOIN_ID, oldHtml, newHtml);

        verify(sysFileRepository).updateJoinRefByObjectNames(JOIN_TABLE, JOIN_ID, List.of("public/202607/550e8400-e29b-41d4-a716-446655440000.jpg"));
    }

    @Test
    void claimHtml_shouldHandleNull() throws Exception {
        assertDoesNotThrow(() -> sysFileService.claimHtml(JOIN_TABLE, JOIN_ID, null, null));
        verify(sysFileRepository, never()).updateJoinRefByObjectNames(any(), any(), any());
    }

    @Test
    void claim_shouldConfirmNewFile() throws Exception {
        sysFileService.claim(JOIN_TABLE, JOIN_ID, null, "public/202607/id-a.jpg");

        verify(sysFileRepository).updateJoinRefByObjectNames(JOIN_TABLE, JOIN_ID, List.of("public/202607/id-a.jpg"));
        verify(sysFileRepository, never()).findByObjectName(any());
    }

    @Test
    void claim_shouldMarkRemovedFilePendingDelete() throws Exception {
        sysFileService.claim(JOIN_TABLE, JOIN_ID, "public/202607/id-a.jpg", null);

        verify(sysFileRepository).updateStatusByObjectNames(List.of("public/202607/id-a.jpg"), FileStatus.PENDING_DELETE);
        verify(sysFileRepository, never()).deleteAllInBatch(any());
        verify(fileOperator, never()).delete(any());
    }

    @Test
    void claim_shouldHandleNulls() throws Exception {
        assertDoesNotThrow(() -> sysFileService.claim(JOIN_TABLE, JOIN_ID, null, null));
        verify(sysFileRepository, never()).updateJoinRefByObjectNames(any(), any(), any());
    }

    @Test
    void deletePhysicalFile_shouldReturnTrueOnSuccess() throws Exception {
        SysFile file = new SysFile("id-a");
        file.setObjectName("public/202607/id-a.jpg");

        boolean ok = sysFileService.deletePhysicalFile(file);

        assertTrue(ok);
        verify(fileOperator).delete("public/202607/id-a.jpg");
    }

    @Test
    void deletePhysicalFile_shouldReturnFalseOnFailure() throws Exception {
        SysFile file = new SysFile("id-a");
        file.setObjectName("public/202607/id-a.jpg");
        doThrow(new RuntimeException("disk error")).when(fileOperator).delete("public/202607/id-a.jpg");

        boolean ok = sysFileService.deletePhysicalFile(file);

        assertFalse(ok);
    }

    @Test
    void deleteByObjectName_shouldMarkPendingDeleteThenDelete() throws Exception {
        SysFile file = new SysFile("id-a");
        file.setObjectName("public/202607/id-a.jpg");
        file.setStatus(FileStatus.IN_USE);
        when(sysFileRepository.findByObjectName("public/202607/id-a.jpg")).thenReturn(file);

        sysFileService.deleteByObjectName("public/202607/id-a.jpg");

        assertEquals(FileStatus.PENDING_DELETE, file.getStatus());
        verify(sysFileRepository).save(file);
        verify(fileOperator).delete("public/202607/id-a.jpg");
        verify(sysFileRepository).deleteById("id-a");
    }

    @Test
    void deleteByObjectName_shouldKeepPendingDeleteWhenPhysicalFails() throws Exception {
        SysFile file = new SysFile("id-a");
        file.setObjectName("public/202607/id-a.jpg");
        file.setStatus(FileStatus.IN_USE);
        when(sysFileRepository.findByObjectName("public/202607/id-a.jpg")).thenReturn(file);
        doThrow(new RuntimeException("disk error")).when(fileOperator).delete("public/202607/id-a.jpg");

        sysFileService.deleteByObjectName("public/202607/id-a.jpg");

        assertEquals(FileStatus.PENDING_DELETE, file.getStatus());
        verify(sysFileRepository).save(file);
        verify(sysFileRepository, never()).deleteById(any());
    }

    @Test
    void deleteByObjectName_shouldDoNothingWhenNotFound() {
        when(sysFileRepository.findByObjectName("public/202607/missing.jpg")).thenReturn(null);

        sysFileService.deleteByObjectName("public/202607/missing.jpg");

        verify(sysFileRepository, never()).deleteById(any());
    }

    @Test
    void deleteFileInternal_shouldNotSaveWhenAlreadyPendingDelete() {
        SysFile file = new SysFile("id-a");
        file.setObjectName("public/202607/id-a.jpg");
        file.setStatus(FileStatus.PENDING_DELETE);

        boolean ok = sysFileService.deleteFileInternal(file);

        assertTrue(ok);
        verify(sysFileRepository, never()).save(any());
        verify(sysFileRepository).deleteById("id-a");
    }
}
