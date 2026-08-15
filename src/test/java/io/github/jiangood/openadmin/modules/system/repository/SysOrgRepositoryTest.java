package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SysOrgRepositoryTest {

    @Autowired
    private SysOrgRepository sysOrgRepository;

    private SysOrg testOrg1;
    private SysOrg testOrg2;

    @BeforeEach
    void setUp() {
        testOrg1 = new SysOrg();
        testOrg1.setName("测试总公司");
        testOrg1.setEnabled(true);
        testOrg1.setType(1);

        sysOrgRepository.save(testOrg1);

        testOrg2 = new SysOrg();
        testOrg2.setName("测试技术部");
        testOrg2.setPid(testOrg1.getId());
        testOrg2.setEnabled(true);
        testOrg2.setType(2);

        sysOrgRepository.save(testOrg2);
    }

    @Test
    void testBasicCrudOperations() {
        SysOrg foundOrg = sysOrgRepository.findById(testOrg1.getId()).orElse(null);
        assertNotNull(foundOrg);
        assertEquals(testOrg1.getName(), foundOrg.getName());

        List<SysOrg> foundOrgs = sysOrgRepository.findAllById(List.of(testOrg1.getId(), testOrg2.getId()));
        assertEquals(2, foundOrgs.size());

        long countBefore = sysOrgRepository.count();
        SysOrg newOrg = new SysOrg();
        newOrg.setName("市场部");
        newOrg.setPid(testOrg1.getId());
        newOrg.setEnabled(true);
        newOrg.setType(2);
        SysOrg savedOrg = sysOrgRepository.save(newOrg);
        assertNotNull(savedOrg.getId());
        assertEquals(countBefore + 1, sysOrgRepository.count());

        sysOrgRepository.delete(savedOrg);
        SysOrg deletedOrg = sysOrgRepository.findById(savedOrg.getId()).orElse(null);
        assertNull(deletedOrg);
    }

    @Test
    void testUpdateField() {
        testOrg1.setName("总公司更新");

        SysOrg db = sysOrgRepository.findById(testOrg1.getId()).orElse(null);
        db.setName(testOrg1.getName());
        sysOrgRepository.save(db);

        SysOrg updatedOrg = sysOrgRepository.findById(testOrg1.getId()).orElse(null);
        assertNotNull(updatedOrg);
        assertEquals("总公司更新", updatedOrg.getName());
    }

    @Test
    void testFieldQueryMethods() {
        SysOrg foundByName = sysOrgRepository.findOne(Spec.<SysOrg>of().eq("name", "测试总公司")).orElse(null);
        assertNotNull(foundByName);
        assertEquals("测试总公司", foundByName.getName());

        List<SysOrg> enabledOrgs = sysOrgRepository.findAll(Spec.<SysOrg>of().eq("enabled", true));
        assertTrue(enabledOrgs.size() >= 2);
    }
}
