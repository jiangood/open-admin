package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.enums.FileStatus;
import io.github.jiangood.openadmin.modules.system.entity.SysFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 这些 @Modifying 查询会被非事务上下文（如 Quartz 定时任务 CleanTempFileJob）直接调用，
 * 因此仓库方法自身必须声明事务，否则抛 "No active transaction for update or delete query"。
 * <p>
 * 本测试故意不加 {@code @Transactional}，以覆盖该非事务调用场景（旧的 {@code SysFileRepositoryTest}
 * 因类级事务会掩盖该缺陷）。
 */
@SpringBootTest
class SysFileRepositoryNonTxRepositoryTest {

    private static final String OBJECT_NAME = "public/202608/non-tx-expired.jpg";
    private static final String OBJECT_NAME_2 = "public/202608/non-tx-object-names.jpg";

    @Autowired
    private SysFileRepository sysFileRepository;

    @AfterEach
    void cleanUp() {
        for (String objectName : List.of(OBJECT_NAME, OBJECT_NAME_2)) {
            SysFile file = sysFileRepository.findByObjectName(objectName);
            if (file != null) {
                sysFileRepository.deleteById(file.getId());
            }
        }
    }

    @Test
    void updateStatusByStatusAndCreateTimeBefore_shouldRunOutsideTransaction() {
        SysFile file = new SysFile();
        file.setObjectName(OBJECT_NAME);
        sysFileRepository.save(file);

        int updated = sysFileRepository.updateStatusByStatusAndCreateTimeBefore(
                FileStatus.TEMP, FileStatus.PENDING_DELETE, LocalDateTime.now().plusMinutes(1));

        assertTrue(updated >= 1);
        assertEquals(FileStatus.PENDING_DELETE, sysFileRepository.findByObjectName(OBJECT_NAME).getStatus());
    }

    @Test
    void updateStatusByObjectNames_shouldRunOutsideTransaction() {
        SysFile file = new SysFile();
        file.setObjectName(OBJECT_NAME_2);
        sysFileRepository.save(file);

        int updated = sysFileRepository.updateStatusByObjectNames(List.of(OBJECT_NAME_2), FileStatus.PENDING_DELETE);

        assertEquals(1, updated);
        assertEquals(FileStatus.PENDING_DELETE, sysFileRepository.findByObjectName(OBJECT_NAME_2).getStatus());
    }
}
