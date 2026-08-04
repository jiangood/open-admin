package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SysFileRepositoryTest {

    @Autowired
    private SysFileRepository sysFileRepository;

    private SysFile saveFile(String objectName) {
        SysFile file = new SysFile();
        file.setObjectName(objectName);
        return sysFileRepository.save(file);
    }

    @Test
    void save_shouldDefaultStatusToTemp() {
        SysFile saved = saveFile("public/202608/temp-default.jpg");
        assertEquals(FileStatus.TEMP, saved.getStatus());
    }

    @Test
    void findByStatus_shouldFilterByStatus() {
        SysFile temp = saveFile("public/202608/a.jpg");
        SysFile inUse = saveFile("public/202608/b.jpg");
        inUse.setJoinTable("sys_article");
        inUse.setJoinId("article-1");
        inUse.setStatus(FileStatus.IN_USE);
        sysFileRepository.save(inUse);

        List<SysFile> inUseFiles = sysFileRepository.findByStatus(FileStatus.IN_USE);
        assertTrue(inUseFiles.stream().anyMatch(f -> f.getId().equals(inUse.getId())));

        List<SysFile> tempFiles = sysFileRepository.findByStatus(FileStatus.TEMP);
        assertTrue(tempFiles.stream().anyMatch(f -> f.getId().equals(temp.getId())));
    }

    @Test
    void findByStatusAndCreateTimeBefore_shouldRespectDeadline() {
        SysFile temp = saveFile("public/202608/deadline.jpg");

        Date future = new Date(System.currentTimeMillis() + 60_000);
        Date past = new Date(System.currentTimeMillis() - 60_000);

        List<SysFile> beforeFuture = sysFileRepository.findByStatusAndCreateTimeBefore(FileStatus.TEMP, future);
        assertTrue(beforeFuture.stream().anyMatch(f -> f.getId().equals(temp.getId())));

        List<SysFile> beforePast = sysFileRepository.findByStatusAndCreateTimeBefore(FileStatus.TEMP, past);
        assertFalse(beforePast.stream().anyMatch(f -> f.getId().equals(temp.getId())));
    }

    @Test
    void updateJoinRefByObjectNames_shouldSetInUseStatus() {
        saveFile("public/202608/claim.jpg");

        sysFileRepository.updateJoinRefByObjectNames("sys_article", "article-1", List.of("public/202608/claim.jpg"));
        sysFileRepository.flush();

        SysFile reloaded = sysFileRepository.findByObjectName("public/202608/claim.jpg");
        assertEquals(FileStatus.IN_USE, reloaded.getStatus());
        assertEquals("sys_article", reloaded.getJoinTable());
        assertEquals("article-1", reloaded.getJoinId());
    }

    @Test
    void updateStatusByObjectNames_shouldSetPendingDelete() {
        saveFile("public/202608/remove.jpg");

        sysFileRepository.updateStatusByObjectNames(List.of("public/202608/remove.jpg"), FileStatus.PENDING_DELETE);
        sysFileRepository.flush();

        SysFile reloaded = sysFileRepository.findByObjectName("public/202608/remove.jpg");
        assertEquals(FileStatus.PENDING_DELETE, reloaded.getStatus());
    }
}
