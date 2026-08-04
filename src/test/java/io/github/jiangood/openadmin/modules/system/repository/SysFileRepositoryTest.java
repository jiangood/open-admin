package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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

        List<SysFile> inUseFiles = sysFileRepository.findByStatus(FileStatus.IN_USE, PageRequest.of(0, 100)).getContent();
        assertTrue(inUseFiles.stream().anyMatch(f -> f.getId().equals(inUse.getId())));

        List<SysFile> tempFiles = sysFileRepository.findByStatus(FileStatus.TEMP, PageRequest.of(0, 100)).getContent();
        assertTrue(tempFiles.stream().anyMatch(f -> f.getId().equals(temp.getId())));
    }

    @Test
    void findByStatus_shouldPageResults() {
        saveFile("public/202608/p1.jpg");
        saveFile("public/202608/p2.jpg");
        saveFile("public/202608/p3.jpg");

        assertEquals(3, sysFileRepository.count());

        assertEquals(2, sysFileRepository.findByStatus(FileStatus.TEMP, PageRequest.of(0, 2)).getContent().size());
        assertEquals(1, sysFileRepository.findByStatus(FileStatus.TEMP, PageRequest.of(1, 2)).getContent().size());
        assertEquals(0, sysFileRepository.findByStatus(FileStatus.TEMP, PageRequest.of(2, 2)).getContent().size());
    }

    @Test
    void updateStatusByStatusAndCreateTimeBefore_shouldMarkOnlyExpiredTemp() {
        SysFile fresh = saveFile("public/202608/fresh.jpg");
        SysFile claimed = saveFile("public/202608/claimed.jpg");
        claimed.setStatus(FileStatus.IN_USE);
        sysFileRepository.save(claimed);

        Date past = new Date(System.currentTimeMillis() - 60_000);
        Date future = new Date(System.currentTimeMillis() + 60_000);

        int updatedPast = sysFileRepository.updateStatusByStatusAndCreateTimeBefore(
                FileStatus.TEMP, FileStatus.PENDING_DELETE, past);
        sysFileRepository.flush();
        assertEquals(0, updatedPast);

        int updatedFuture = sysFileRepository.updateStatusByStatusAndCreateTimeBefore(
                FileStatus.TEMP, FileStatus.PENDING_DELETE, future);
        sysFileRepository.flush();
        assertEquals(1, updatedFuture);

        assertEquals(FileStatus.PENDING_DELETE, sysFileRepository.findByObjectName("public/202608/fresh.jpg").getStatus());
        assertEquals(FileStatus.IN_USE, sysFileRepository.findByObjectName("public/202608/claimed.jpg").getStatus());
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
