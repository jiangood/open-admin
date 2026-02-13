package io.github.jiangood.openadmin.modules.system.dao;

import io.github.jiangood.openadmin.modules.system.entity.SysManual;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class SysManualDaoTest {

    @Autowired
    private SysManualDao sysManualDao;

    @Test
    void testSave() {
        SysManual manual = new SysManual();
        manual.setName("Test Manual");
        manual.setVersion(1);
        manual.setFileId("file123");

        SysManual savedManual = sysManualDao.save(manual);
        assertNotNull(savedManual.getId());
        assertEquals("Test Manual", savedManual.getName());
        assertEquals(1, savedManual.getVersion());
        assertEquals("file123", savedManual.getFileId());
    }

    @Test
    void testFindById() {
        SysManual manual = new SysManual();
        manual.setName("Test Manual");
        manual.setVersion(1);
        manual.setFileId("file123");

        SysManual savedManual = sysManualDao.save(manual);
        SysManual foundManual = sysManualDao.findById(savedManual.getId()).orElse(null);
        assertNotNull(foundManual);
        assertEquals(savedManual.getId(), foundManual.getId());
    }

    @Test
    void testDeleteById() {
        SysManual manual = new SysManual();
        manual.setName("Test Manual");
        manual.setVersion(1);
        manual.setFileId("file123");

        SysManual savedManual = sysManualDao.save(manual);
        sysManualDao.deleteById(savedManual.getId());
        SysManual foundManual = sysManualDao.findById(savedManual.getId()).orElse(null);
        assertNull(foundManual);
    }

    @Test
    void testFindAll() {
        for (int i = 1; i <= 3; i++) {
            SysManual manual = new SysManual();
            manual.setName("Test Manual " + i);
            manual.setVersion(i);
            manual.setFileId("file" + i);
            sysManualDao.save(manual);
        }

        List<SysManual> manuals = sysManualDao.findAll();
        assertTrue(manuals.size() >= 3);
    }

    @Test
    void testUpdate() {
        SysManual manual = new SysManual();
        manual.setName("Test Manual");
        manual.setVersion(1);
        manual.setFileId("file123");

        SysManual savedManual = sysManualDao.save(manual);
        savedManual.setFileId("updatedFile123");

        SysManual updatedManual = sysManualDao.save(savedManual);
        assertEquals("updatedFile123", updatedManual.getFileId());
    }

    @Test
    void testFindMaxVersion() {
        // 测试不存在的名称
        int maxVersion1 = sysManualDao.findMaxVersion("Non Existent Manual");
        assertEquals(0, maxVersion1);

        // 测试只有一个版本的情况
        SysManual manual1 = new SysManual();
        manual1.setName("Test Manual");
        manual1.setVersion(1);
        manual1.setFileId("file1");
        sysManualDao.save(manual1);

        int maxVersion2 = sysManualDao.findMaxVersion("Test Manual");
        assertEquals(1, maxVersion2);

        // 测试多个版本的情况
        SysManual manual2 = new SysManual();
        manual2.setName("Test Manual");
        manual2.setVersion(2);
        manual2.setFileId("file2");
        sysManualDao.save(manual2);

        SysManual manual3 = new SysManual();
        manual3.setName("Test Manual");
        manual3.setVersion(3);
        manual3.setFileId("file3");
        sysManualDao.save(manual3);

        int maxVersion3 = sysManualDao.findMaxVersion("Test Manual");
        assertEquals(3, maxVersion3);

        // 测试不同名称的情况
        SysManual manual4 = new SysManual();
        manual4.setName("Another Manual");
        manual4.setVersion(5);
        manual4.setFileId("file4");
        sysManualDao.save(manual4);

        int maxVersion4 = sysManualDao.findMaxVersion("Another Manual");
        assertEquals(5, maxVersion4);

        // 再次测试第一个名称，确保不受其他名称影响
        int maxVersion5 = sysManualDao.findMaxVersion("Test Manual");
        assertEquals(3, maxVersion5);
    }
}
