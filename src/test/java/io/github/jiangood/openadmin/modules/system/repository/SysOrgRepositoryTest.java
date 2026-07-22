package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SysOrgRepositoryTest {

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
        SysOrg foundOrg = sysOrgRepository.findOne(testOrg1.getId());
        assertNotNull(foundOrg);
        assertEquals(testOrg1.getName(), foundOrg.getName());

        String[] ids = {testOrg1.getId(), testOrg2.getId()};
        List<SysOrg> foundOrgs = sysOrgRepository.findAllById(ids);
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
        SysOrg deletedOrg = sysOrgRepository.findOne(savedOrg.getId());
        assertNull(deletedOrg);
    }

    @Test
    void testBatchOperations() {
        SysOrg org3 = new SysOrg();
        org3.setName("财务部");
        org3.setPid(testOrg1.getId());
        org3.setEnabled(true);
        org3.setType(2);

        SysOrg org4 = new SysOrg();
        org4.setName("人力资源部");
        org4.setPid(testOrg1.getId());
        org4.setEnabled(true);
        org4.setType(2);

        List<SysOrg> batchOrgs = Arrays.asList(org3, org4);
        List<SysOrg> savedBatchOrgs = sysOrgRepository.saveAllBatch(batchOrgs);
        assertEquals(2, savedBatchOrgs.size());
        assertNotNull(savedBatchOrgs.get(0).getId());
        assertNotNull(savedBatchOrgs.get(1).getId());

        List<String> idsToDelete = Arrays.asList(org3.getId(), org4.getId());
        long countBefore = sysOrgRepository.count();
        sysOrgRepository.deleteAllBatch(idsToDelete);
        assertEquals(countBefore - 2, sysOrgRepository.count());
    }

    @Test
    void testUpdateFieldMethods() {
        testOrg1.setName("总公司更新");
        sysOrgRepository.updateField(testOrg1, Arrays.asList("name"));

        SysOrg updatedOrg = sysOrgRepository.findOne(testOrg1.getId());
        assertNotNull(updatedOrg);
        assertEquals("总公司更新", updatedOrg.getName());

        testOrg1.setName("总公司直接更新");
        sysOrgRepository.updateFieldDirect(testOrg1, Arrays.asList("name"));

        SysOrg directlyUpdatedOrg = sysOrgRepository.findOne(testOrg1.getId());
        assertNotNull(directlyUpdatedOrg);
        assertEquals("总公司直接更新", directlyUpdatedOrg.getName());
    }

    @Test
    void testFieldQueryMethods() {
        SysOrg foundByName = sysOrgRepository.findByField("name", "测试总公司");
        assertNotNull(foundByName);
        assertEquals("测试总公司", foundByName.getName());

        List<SysOrg> enabledOrgs = sysOrgRepository.findAllByField("enabled", true);
        assertTrue(enabledOrgs.size() >= 2);
    }
}
