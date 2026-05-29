package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.modules.system.entity.SysRole;
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
public class SysRoleRepositoryTest {

    @Autowired
    private SysRoleRepository sysRoleRepository;

    private SysRole testRole1;
    private SysRole testRole2;

    @BeforeEach
    void setUp() {
        testRole1 = new SysRole();
        testRole1.setName("测试管理员角色");
        testRole1.setCode("TEST_ADMIN");
        testRole1.setEnabled(true);

        testRole2 = new SysRole();
        testRole2.setName("测试普通用户角色");
        testRole2.setCode("TEST_USER");
        testRole2.setEnabled(true);

        sysRoleRepository.save(testRole1);
        sysRoleRepository.save(testRole2);
    }

    @Test
    void testBasicCrudOperations() {
        SysRole foundRole = sysRoleRepository.findOne(testRole1.getId());
        assertNotNull(foundRole);
        assertEquals(testRole1.getName(), foundRole.getName());

        String[] ids = {testRole1.getId(), testRole2.getId()};
        List<SysRole> foundRoles = sysRoleRepository.findAllById(ids);
        assertEquals(2, foundRoles.size());

        long countBefore = sysRoleRepository.count();
        SysRole newRole = new SysRole();
        newRole.setName("新角色");
        newRole.setCode("NEW_ROLE");
        newRole.setEnabled(true);
        SysRole savedRole = sysRoleRepository.save(newRole);
        assertNotNull(savedRole.getId());
        assertEquals(countBefore + 1, sysRoleRepository.count());

        sysRoleRepository.delete(savedRole);
        SysRole deletedRole = sysRoleRepository.findOne(savedRole.getId());
        assertNull(deletedRole);
    }

    @Test
    void testBatchOperations() {
        SysRole role3 = new SysRole();
        role3.setName("角色3");
        role3.setCode("ROLE3");
        role3.setEnabled(true);

        SysRole role4 = new SysRole();
        role4.setName("角色4");
        role4.setCode("ROLE4");
        role4.setEnabled(true);

        List<SysRole> batchRoles = Arrays.asList(role3, role4);
        List<SysRole> savedBatchRoles = sysRoleRepository.saveAllBatch(batchRoles);
        assertEquals(2, savedBatchRoles.size());
        assertNotNull(savedBatchRoles.get(0).getId());
        assertNotNull(savedBatchRoles.get(1).getId());

        List<String> idsToDelete = Arrays.asList(role3.getId(), role4.getId());
        long countBefore = sysRoleRepository.count();
        sysRoleRepository.deleteAllBatch(idsToDelete);
        assertEquals(countBefore - 2, sysRoleRepository.count());
    }

    @Test
    void testUpdateFieldMethods() {
        testRole1.setName("管理员角色更新");
        testRole1.setCode("ADMIN_UPDATED");
        sysRoleRepository.updateField(testRole1, Arrays.asList("name", "code"));

        SysRole updatedRole = sysRoleRepository.findOne(testRole1.getId());
        assertNotNull(updatedRole);
        assertEquals("管理员角色更新", updatedRole.getName());
        assertEquals("ADMIN_UPDATED", updatedRole.getCode());

        testRole1.setName("管理员角色直接更新");
        sysRoleRepository.updateFieldDirect(testRole1, Arrays.asList("name"));

        SysRole directlyUpdatedRole = sysRoleRepository.findOne(testRole1.getId());
        assertNotNull(directlyUpdatedRole);
        assertEquals("管理员角色直接更新", directlyUpdatedRole.getName());
    }

    @Test
    void testFieldQueryMethods() {
        SysRole foundByCode = sysRoleRepository.findByField("code", "TEST_ADMIN");
        assertNotNull(foundByCode);
        assertEquals("测试管理员角色", foundByCode.getName());

        List<SysRole> enabledRoles = sysRoleRepository.findAllByField("enabled", true);
        assertTrue(enabledRoles.size() >= 2);
    }
}
