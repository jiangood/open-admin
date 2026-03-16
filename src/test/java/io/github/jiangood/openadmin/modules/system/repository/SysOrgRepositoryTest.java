package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.modules.system.entity.SysOrg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SysOrgRepositoryTest {

    @Autowired
    private SysOrgRepository sysOrgRepository;

    private SysOrg testOrg1;
    private SysOrg testOrg2;

    @BeforeEach
    void setUp() {
        // 清空数据
        sysOrgRepository.deleteAll();

        // 准备测试数据
        testOrg1 = new SysOrg();
        testOrg1.setName("总公司");
        testOrg1.setEnabled(true);
        testOrg1.setType(1); // 1 表示公司类型

        sysOrgRepository.save(testOrg1);

        testOrg2 = new SysOrg();
        testOrg2.setName("技术部");
        testOrg2.setPid(testOrg1.getId());
        testOrg2.setEnabled(true);
        testOrg2.setType(2); // 2 表示部门类型

        sysOrgRepository.save(testOrg2);
    }

    // 测试基本CRUD操作
    @Test
    void testBasicCrudOperations() {
        // 测试findOne
        SysOrg foundOrg = sysOrgRepository.findOne(testOrg1.getId());
        assertNotNull(foundOrg);
        assertEquals(testOrg1.getName(), foundOrg.getName());

        // 测试findAllById
        String[] ids = {testOrg1.getId(), testOrg2.getId()};
        List<SysOrg> foundOrgs = sysOrgRepository.findAllById(ids);
        assertEquals(2, foundOrgs.size());

        // 测试count
        long count = sysOrgRepository.count();
        assertEquals(2, count);

        // 测试save
        SysOrg newOrg = new SysOrg();
        newOrg.setName("市场部");
        newOrg.setPid(testOrg1.getId());
        newOrg.setEnabled(true);
        newOrg.setType(2); // 2 表示部门类型
        SysOrg savedOrg = sysOrgRepository.save(newOrg);
        assertNotNull(savedOrg.getId());

        // 测试delete
        sysOrgRepository.delete(savedOrg);
        SysOrg deletedOrg = sysOrgRepository.findOne(savedOrg.getId());
        assertNull(deletedOrg);
    }

    // 测试批量操作
    @Test
    void testBatchOperations() {
        // 测试saveAllBatch
        SysOrg org3 = new SysOrg();
        org3.setName("财务部");
        org3.setPid(testOrg1.getId());
        org3.setEnabled(true);
        org3.setType(2); // 2 表示部门类型

        SysOrg org4 = new SysOrg();
        org4.setName("人力资源部");
        org4.setPid(testOrg1.getId());
        org4.setEnabled(true);
        org4.setType(2); // 2 表示部门类型

        List<SysOrg> batchOrgs = Arrays.asList(org3, org4);
        List<SysOrg> savedBatchOrgs = sysOrgRepository.saveAllBatch(batchOrgs);
        assertEquals(2, savedBatchOrgs.size());
        assertNotNull(savedBatchOrgs.get(0).getId());
        assertNotNull(savedBatchOrgs.get(1).getId());

        // 测试deleteAllBatch
        List<String> idsToDelete = Arrays.asList(org3.getId(), org4.getId());
        sysOrgRepository.deleteAllBatch(idsToDelete);

        SysOrg deletedOrg3 = sysOrgRepository.findOne(org3.getId());
        SysOrg deletedOrg4 = sysOrgRepository.findOne(org4.getId());
        assertNull(deletedOrg3);
        assertNull(deletedOrg4);
    }

    // 测试字段更新方法
    @Test
    void testUpdateFieldMethods() {
        // 测试updateField
        testOrg1.setName("总公司更新");
        sysOrgRepository.updateField(testOrg1, Arrays.asList("name"));

        SysOrg updatedOrg = sysOrgRepository.findOne(testOrg1.getId());
        assertNotNull(updatedOrg);
        assertEquals("总公司更新", updatedOrg.getName());

        // 测试updateFieldDirect
        testOrg1.setName("总公司直接更新");
        sysOrgRepository.updateFieldDirect(testOrg1, Arrays.asList("name"));

        SysOrg directlyUpdatedOrg = sysOrgRepository.findOne(testOrg1.getId());
        assertNotNull(directlyUpdatedOrg);
        assertEquals("总公司直接更新", directlyUpdatedOrg.getName());
    }

    // 测试字段查询方法
    @Test
    void testFieldQueryMethods() {
        // 测试findByField
        SysOrg foundByName = sysOrgRepository.findByField("name", "总公司");
        assertNotNull(foundByName);
        assertEquals("总公司", foundByName.getName());

        // 测试findAllByField
        List<SysOrg> enabledOrgs = sysOrgRepository.findAllByField("enabled", true);
        assertEquals(2, enabledOrgs.size());
    }
}
