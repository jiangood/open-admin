package io.github.jiangood.openadmin.modules.system.repository;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SysRoleRepositoryTest {

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
        SysRole foundRole = sysRoleRepository.findById(testRole1.getId()).orElse(null);
        assertNotNull(foundRole);
        assertEquals(testRole1.getName(), foundRole.getName());

        List<SysRole> foundRoles = sysRoleRepository.findAllById(List.of(testRole1.getId(), testRole2.getId()));
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
        SysRole deletedRole = sysRoleRepository.findById(savedRole.getId()).orElse(null);
        assertNull(deletedRole);
    }

    @Test
    void testUpdateField() {
        testRole1.setName("管理员角色更新");
        testRole1.setCode("ADMIN_UPDATED");

        SysRole db = sysRoleRepository.findById(testRole1.getId()).orElse(null);
        db.setName(testRole1.getName());
        db.setCode(testRole1.getCode());
        sysRoleRepository.save(db);

        SysRole updatedRole = sysRoleRepository.findById(testRole1.getId()).orElse(null);
        assertNotNull(updatedRole);
        assertEquals("管理员角色更新", updatedRole.getName());
        assertEquals("ADMIN_UPDATED", updatedRole.getCode());
    }

    @Test
    void testFieldQueryMethods() {
        SysRole foundByCode = sysRoleRepository.findOne(Spec.<SysRole>of().eq("code", "TEST_ADMIN")).orElse(null);
        assertNotNull(foundByCode);
        assertEquals("测试管理员角色", foundByCode.getName());

        List<SysRole> enabledRoles = sysRoleRepository.findAll(Spec.<SysRole>of().eq("enabled", true));
        assertTrue(enabledRoles.size() >= 2);
    }
}
