package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SysRoleRepositoryTest {

    @Autowired
    private SysRoleRepository sysRoleRepository;

    private SysRole testRole1;
    private SysRole testRole2;

    @BeforeEach
    void setUp() {
        // 清空数据
        sysRoleRepository.deleteAll();

        // 准备测试数据
        testRole1 = new SysRole();
        testRole1.setName("管理员角色");
        testRole1.setCode("ADMIN");
        testRole1.setEnabled(true);

        testRole2 = new SysRole();
        testRole2.setName("普通用户角色");
        testRole2.setCode("USER");
        testRole2.setEnabled(true);

        sysRoleRepository.save(testRole1);
        sysRoleRepository.save(testRole2);
    }

    // 测试基本CRUD操作
    @Test
    void testBasicCrudOperations() {
        // 测试findOne
        SysRole foundRole = sysRoleRepository.findOne(testRole1.getId());
        assertNotNull(foundRole);
        assertEquals(testRole1.getName(), foundRole.getName());

        // 测试findAllById
        String[] ids = {testRole1.getId(), testRole2.getId()};
        List<SysRole> foundRoles = sysRoleRepository.findAllById(ids);
        assertEquals(2, foundRoles.size());

        // 测试count
        long count = sysRoleRepository.count();
        assertEquals(2, count);

        // 测试save
        SysRole newRole = new SysRole();
        newRole.setName("新角色");
        newRole.setCode("NEW_ROLE");
        newRole.setEnabled(true);
        SysRole savedRole = sysRoleRepository.save(newRole);
        assertNotNull(savedRole.getId());

        // 测试delete
        sysRoleRepository.delete(savedRole);
        SysRole deletedRole = sysRoleRepository.findOne(savedRole.getId());
        assertNull(deletedRole);
    }

    // 测试批量操作
    @Test
    void testBatchOperations() {
        // 测试saveAllBatch
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

        // 测试deleteAllBatch
        List<String> idsToDelete = Arrays.asList(role3.getId(), role4.getId());
        sysRoleRepository.deleteAllBatch(idsToDelete);

        SysRole deletedRole3 = sysRoleRepository.findOne(role3.getId());
        SysRole deletedRole4 = sysRoleRepository.findOne(role4.getId());
        assertNull(deletedRole3);
        assertNull(deletedRole4);
    }

    // 测试字段更新方法
    @Test
    void testUpdateFieldMethods() {
        // 测试updateField
        testRole1.setName("管理员角色更新");
        testRole1.setCode("ADMIN_UPDATED");
        sysRoleRepository.updateField(testRole1, Arrays.asList("name", "code"));

        SysRole updatedRole = sysRoleRepository.findOne(testRole1.getId());
        assertNotNull(updatedRole);
        assertEquals("管理员角色更新", updatedRole.getName());
        assertEquals("ADMIN_UPDATED", updatedRole.getCode());

        // 测试updateFieldDirect
        testRole1.setName("管理员角色直接更新");
        sysRoleRepository.updateFieldDirect(testRole1, Arrays.asList("name"));

        SysRole directlyUpdatedRole = sysRoleRepository.findOne(testRole1.getId());
        assertNotNull(directlyUpdatedRole);
        assertEquals("管理员角色直接更新", directlyUpdatedRole.getName());
    }

    // 测试字段查询方法
    @Test
    void testFieldQueryMethods() {
        // 测试findByField
        SysRole foundByCode = sysRoleRepository.findByField("code", "ADMIN");
        assertNotNull(foundByCode);
        assertEquals("管理员角色", foundByCode.getName());

        // 测试findAllByField
        List<SysRole> enabledRoles = sysRoleRepository.findAllByField("enabled", true);
        assertEquals(2, enabledRoles.size());
    }
}
