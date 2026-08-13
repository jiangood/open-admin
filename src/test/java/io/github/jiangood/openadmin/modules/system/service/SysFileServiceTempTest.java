package io.github.jiangood.openadmin.modules.system.service;

import io.github.jiangood.openadmin.framework.config.SystemProperties;
import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.framework.spi.FileOperator;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import io.github.jiangood.openadmin.modules.system.repository.SysFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

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
    void claimHtml_shouldExtractImgDirectoryObjectNames() throws Exception {
        String newHtml = "<p><img src=\"/file/public/img/202607/550e8400-e29b-41d4-a716-446655440000.jpg\"></p>"
                + "<p><img src=\"/file/private/img/202607/550e8400-e29b-41d4-a716-446655440000.jpg\"></p>";

        sysFileService.claimHtml(JOIN_TABLE, JOIN_ID, null, newHtml);

        verify(sysFileRepository).updateJoinRefByObjectNames(JOIN_TABLE, JOIN_ID, List.of(
                "public/img/202607/550e8400-e29b-41d4-a716-446655440000.jpg",
                "private/img/202607/550e8400-e29b-41d4-a716-446655440000.jpg"));
    }

    @Test
    void claimHtml_shouldStripQueryStringAndContextPath() throws Exception {
        String newHtml = "<img src=\"/example/file/public/img/202607/550e8400-e29b-41d4-a716-446655440000.jpg?thumb=1\">";

        sysFileService.claimHtml(JOIN_TABLE, JOIN_ID, null, newHtml);

        verify(sysFileRepository).updateJoinRefByObjectNames(JOIN_TABLE, JOIN_ID,
                List.of("public/img/202607/550e8400-e29b-41d4-a716-446655440000.jpg"));
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
    void deleteBatch_shouldReturnZeroOnEmptyIds() {
        assertEquals(0, sysFileService.deleteBatch(List.of()));
        assertEquals(0, sysFileService.deleteBatch(null));
        verify(sysFileRepository, never()).findAllById(any());
    }

    @Test
    void deleteBatch_shouldDeleteAllFoundFiles() throws Exception {
        SysFile fileA = new SysFile("id-a");
        fileA.setObjectName("public/202607/id-a.jpg");
        fileA.setStatus(FileStatus.IN_USE);
        SysFile fileB = new SysFile("id-b");
        fileB.setObjectName("public/202607/id-b.jpg");
        fileB.setStatus(FileStatus.IN_USE);
        when(sysFileRepository.findAllById(List.of("id-a", "id-b"))).thenReturn(List.of(fileA, fileB));

        int success = sysFileService.deleteBatch(List.of("id-a", "id-b"));

        assertEquals(2, success);
        verify(fileOperator).delete("public/202607/id-a.jpg");
        verify(fileOperator).delete("public/202607/id-b.jpg");
        verify(sysFileRepository).deleteById("id-a");
        verify(sysFileRepository).deleteById("id-b");
    }

    @Test
    void deleteBatch_shouldSkipNotFoundAndKeepPendingWhenPhysicalFails() throws Exception {
        SysFile fileA = new SysFile("id-a");
        fileA.setObjectName("public/202607/id-a.jpg");
        fileA.setStatus(FileStatus.IN_USE);
        SysFile fileB = new SysFile("id-b");
        fileB.setObjectName("public/202607/id-b.jpg");
        fileB.setStatus(FileStatus.IN_USE);
        when(sysFileRepository.findAllById(List.of("id-a", "id-b", "missing"))).thenReturn(List.of(fileA, fileB));
        lenient().doThrow(new RuntimeException("disk error")).when(fileOperator).delete("public/202607/id-b.jpg");

        int success = sysFileService.deleteBatch(List.of("id-a", "id-b", "missing"));

        assertEquals(1, success);
        assertEquals(FileStatus.PENDING_DELETE, fileB.getStatus());
        verify(sysFileRepository).deleteById("id-a");
        verify(sysFileRepository, never()).deleteById("id-b");
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

    @Test
    void deleteFileInternal_shouldKeepPendingDeleteWhenDbDeleteFails() throws Exception {
        SysFile file = new SysFile("id-a");
        file.setObjectName("public/202607/id-a.jpg");
        file.setStatus(FileStatus.IN_USE);
        doThrow(new RuntimeException("db error")).when(sysFileRepository).deleteById("id-a");

        boolean ok = sysFileService.deleteFileInternal(file);

        assertFalse(ok);
        assertEquals(FileStatus.PENDING_DELETE, file.getStatus());
        verify(sysFileRepository).save(file);
        verify(fileOperator).delete("public/202607/id-a.jpg");
        verify(sysFileRepository).deleteById("id-a");
    }

    @Test
    void thumbKeyOf_shouldInsertThumbMarkBeforeSuffix() {
        assertEquals("public/img/202607/id-a.thumb.jpg", SysFileService.thumbKeyOf("public/img/202607/id-a.jpg"));
    }

    @Test
    void deletePhysicalFile_shouldAlsoDeleteThumbnail() throws Exception {
        SysFile file = new SysFile("id-a");
        file.setObjectName("public/img/202607/id-a.jpg");

        boolean ok = sysFileService.deletePhysicalFile(file);

        assertTrue(ok);
        verify(fileOperator).delete("public/img/202607/id-a.jpg");
        verify(fileOperator).delete("public/img/202607/id-a.thumb.jpg");
    }

    @Test
    void deletePhysicalFile_shouldIgnoreThumbnailDeleteFailure() throws Exception {
        SysFile file = new SysFile("id-a");
        file.setObjectName("public/img/202607/id-a.jpg");
        lenient().doThrow(new RuntimeException("thumb disk error")).when(fileOperator).delete("public/img/202607/id-a.thumb.jpg");

        boolean ok = sysFileService.deletePhysicalFile(file);

        assertTrue(ok);
        verify(fileOperator).delete("public/img/202607/id-a.jpg");
        verify(fileOperator).delete("public/img/202607/id-a.thumb.jpg");
    }

    @Test
    void uploadImage_shouldGenerateImgDirectoryObjectNameAndSaveBoth() throws Exception {
        when(systemProperties.getFile()).thenReturn(new SystemProperties.FileStorage());

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, 0x00, 0x10});
        MockMultipartFile thumb = new MockMultipartFile("thumb", "photo.thumb.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, 0x00, 0x10});
        when(sysFileRepository.save(any(SysFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SysFile result = sysFileService.uploadImage(file, thumb, true);

        ArgumentCaptor<String> mainKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> thumbKey = ArgumentCaptor.forClass(String.class);
        verify(fileOperator, atLeastOnce()).saveFile(mainKey.capture(), any());
        verify(fileOperator, atLeastOnce()).saveFile(thumbKey.capture(), any());

        assertEquals("public/img/", result.getObjectName().substring(0, 11));
        assertTrue(result.getObjectName().endsWith(".jpg"));
        assertEquals("image", result.getType());
        assertEquals(SysFileService.thumbKeyOf(result.getObjectName()),
                thumbKey.getAllValues().stream().filter(k -> k.contains(".thumb.")).findFirst().orElse(null));
    }
}
